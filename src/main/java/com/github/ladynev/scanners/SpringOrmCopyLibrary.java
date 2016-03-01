package com.github.ladynev.scanners;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipException;

import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class SpringOrmCopyLibrary extends ScannerAdapter {

    private static final String RESOURCE_PATTERN = "/**/*.class";

    private static final String CLASSPATH_ALL_URL_PREFIX = "classpath*:";

    private final PathMatcher pathMatcher = new AntPathMatcher();

    private ResourceLoader resourceLoader;

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        final List<Class<?>> result = new ArrayList<Class<?>>();

        AnnotationTypeFilter filter = new AnnotationTypeFilter(getAnnotation(), false);

        resourceLoader = new DefaultResourceLoader(getLoader());

        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX
                + ClassUtils.convertClassNameToResourcePath(packageToScan) + RESOURCE_PATTERN;

        Resource[] resources = getResources(pattern);

        SimpleMetadataReaderFactory readerFactory = new SimpleMetadataReaderFactory(resourceLoader);

        for (Resource resource : resources) {
            if (resource.isReadable()) {
                MetadataReader reader = readerFactory.getMetadataReader(resource);
                String className = reader.getClassMetadata().getClassName();
                if (filter.match(reader, readerFactory)) {
                    result.add(ClassUtils.resolveClassName(className, getLoader()));
                }
            }
        }

        return result;
    }

    private Resource[] getResources(String locationPattern) throws IOException {
        // a class path resource (multiple resources for same name possible)
        if (pathMatcher.isPattern(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length()))) {
            // a class path resource pattern
            return findPathMatchingResources(locationPattern);
        } else {
            // all class path resources with the given name
            return findAllClassPathResources(locationPattern.substring(CLASSPATH_ALL_URL_PREFIX
                    .length()));
        }
    }

    private Resource[] findPathMatchingResources(String locationPattern) throws IOException {
        String rootDirPath = determineRootDir(locationPattern);
        String subPattern = locationPattern.substring(rootDirPath.length());
        Resource[] rootDirResources = getResources(rootDirPath);
        Set<Resource> result = new LinkedHashSet<Resource>(16);
        for (Resource rootDirResource : rootDirResources) {
            if (isJarResource(rootDirResource)) {
                result.addAll(doFindPathMatchingJarResources(rootDirResource, subPattern));
            } else {
                result.addAll(doFindPathMatchingFileResources(rootDirResource, subPattern));
            }
        }

        return result.toArray(new Resource[result.size()]);
    }

    private Resource[] findAllClassPathResources(String location) throws IOException {
        String path = location;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        Set<Resource> result = doFindAllClassPathResources(path);
        return result.toArray(new Resource[result.size()]);
    }

    private Set<Resource> doFindAllClassPathResources(String path) throws IOException {
        Set<Resource> result = new LinkedHashSet<Resource>(16);
        ClassLoader cl = resourceLoader.getClassLoader();
        Enumeration<URL> resourceUrls = cl != null ? cl.getResources(path) : ClassLoader
                .getSystemResources(path);
        while (resourceUrls.hasMoreElements()) {
            URL url = resourceUrls.nextElement();
            result.add(convertClassLoaderURL(url));
        }
        if ("".equals(path)) {
            // The above result is likely to be incomplete, i.e. only containing file system
            // references.
            // We need to have pointers to each of the jar files on the classpath as well...
            addAllClassLoaderJarRoots(cl, result);
        }
        return result;
    }

    private Resource convertClassLoaderURL(URL url) {
        return new UrlResource(url);
    }

    private String determineRootDir(String location) {
        int prefixEnd = location.indexOf(":") + 1;
        int rootDirEnd = location.length();
        while (rootDirEnd > prefixEnd
                && pathMatcher.isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1;
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd;
        }
        return location.substring(0, rootDirEnd);
    }

    private boolean isJarResource(Resource resource) throws IOException {
        return ResourceUtils.isJarURL(resource.getURL());
    }

    private Set<Resource> doFindPathMatchingJarResources(Resource rootDirResource, String subPattern)
            throws IOException {

        URLConnection con = rootDirResource.getURL().openConnection();
        JarFile jarFile;
        String jarFileUrl;
        String rootEntryPath;
        boolean newJarFile = false;

        if (con instanceof JarURLConnection) {
            // Should usually be the case for traditional JAR files.
            JarURLConnection jarCon = (JarURLConnection) con;
            ResourceUtils.useCachesIfNecessary(jarCon);
            jarFile = jarCon.getJarFile();
            jarFileUrl = jarCon.getJarFileURL().toExternalForm();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = jarEntry != null ? jarEntry.getName() : "";
        } else {
            // No JarURLConnection -> need to resort to URL file parsing.
            // We'll assume URLs of the format "jar:path!/entry", with the protocol
            // being arbitrary as long as following the entry format.
            // We'll also handle paths with and without leading "file:" prefix.
            String urlFile = rootDirResource.getURL().getFile();
            try {
                int separatorIndex = urlFile.indexOf(ResourceUtils.JAR_URL_SEPARATOR);
                if (separatorIndex != -1) {
                    jarFileUrl = urlFile.substring(0, separatorIndex);
                    rootEntryPath = urlFile.substring(separatorIndex
                            + ResourceUtils.JAR_URL_SEPARATOR.length());
                    jarFile = getJarFile(jarFileUrl);
                } else {
                    jarFile = new JarFile(urlFile);
                    jarFileUrl = urlFile;
                    rootEntryPath = "";
                }
                newJarFile = true;
            } catch (ZipException ex) {
                System.out.println("Skipping invalid jar classpath entry [" + urlFile + "]");
                return Collections.emptySet();
            }
        }

        try {
            if (!"".equals(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                // Root entry path must end with slash to allow for proper matching.
                // The Sun JRE does not return a slash here, but BEA JRockit does.
                rootEntryPath = rootEntryPath + "/";
            }
            Set<Resource> result = new LinkedHashSet<Resource>(8);
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    String relativePath = entryPath.substring(rootEntryPath.length());
                    if (pathMatcher.match(subPattern, relativePath)) {
                        result.add(rootDirResource.createRelative(relativePath));
                    }
                }
            }
            return result;
        } finally {
            // Close jar file, but only if freshly obtained -
            // not from JarURLConnection, which might cache the file reference.
            if (newJarFile) {
                jarFile.close();
            }
        }
    }

    /**
     * Resolve the given jar file URL into a JarFile object.
     */
    private JarFile getJarFile(String jarFileUrl) throws IOException {
        if (jarFileUrl.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
            try {
                return new JarFile(ResourceUtils.toURI(jarFileUrl).getSchemeSpecificPart());
            } catch (URISyntaxException ex) {
                // Fallback for URLs that are not valid URIs (should hardly ever happen).
                return new JarFile(jarFileUrl.substring(ResourceUtils.FILE_URL_PREFIX.length()));
            }
        } else {
            return new JarFile(jarFileUrl);
        }
    }

    /**
     * Find all resources in the file system that match the given location pattern via the Ant-style
     * PathMatcher.
     *
     * @param rootDirResource
     *            the root directory as Resource
     * @param subPattern
     *            the sub pattern to match (below the root directory)
     * @return a mutable Set of matching Resource instances
     * @throws IOException
     *             in case of I/O errors
     * @see #retrieveMatchingFiles
     * @see org.springframework.util.PathMatcher
     */
    private Set<Resource> doFindPathMatchingFileResources(Resource rootDirResource,
            String subPattern) throws IOException {

        File rootDir;
        try {
            rootDir = rootDirResource.getFile().getAbsoluteFile();
        } catch (IOException ex) {
            System.out.println("Cannot search for matching files underneath " + rootDirResource
                    + " because it does not correspond to a directory in the file system");
            ex.printStackTrace();
            return Collections.emptySet();
        }
        return doFindMatchingFileSystemResources(rootDir, subPattern);
    }

    /**
     * Find all resources in the file system that match the given location pattern via the Ant-style
     * PathMatcher.
     *
     * @param rootDir
     *            the root directory in the file system
     * @param subPattern
     *            the sub pattern to match (below the root directory)
     * @return a mutable Set of matching Resource instances
     * @throws IOException
     *             in case of I/O errors
     * @see #retrieveMatchingFiles
     * @see org.springframework.util.PathMatcher
     */
    private Set<Resource> doFindMatchingFileSystemResources(File rootDir, String subPattern)
            throws IOException {
        Set<File> matchingFiles = retrieveMatchingFiles(rootDir, subPattern);
        Set<Resource> result = new LinkedHashSet<Resource>(matchingFiles.size());
        for (File file : matchingFiles) {
            result.add(new FileSystemResource(file));
        }
        return result;
    }

    /**
     * Retrieve files that match the given path pattern, checking the given directory and its
     * subdirectories.
     *
     * @param rootDir
     *            the directory to start from
     * @param pattern
     *            the pattern to match against, relative to the root directory
     * @return a mutable Set of matching Resource instances
     * @throws IOException
     *             if directory contents could not be retrieved
     */
    private Set<File> retrieveMatchingFiles(File rootDir, String pattern) throws IOException {
        if (!rootDir.exists()) {
            // Silently skip non-existing directories.
            return Collections.emptySet();
        }
        if (!rootDir.isDirectory()) {
            // Complain louder if it exists but is no directory.
            return Collections.emptySet();
        }
        if (!rootDir.canRead()) {
            return Collections.emptySet();
        }
        String fullPattern = StringUtils.replace(rootDir.getAbsolutePath(), File.separator, "/");
        if (!pattern.startsWith("/")) {
            fullPattern += "/";
        }
        fullPattern = fullPattern + StringUtils.replace(pattern, File.separator, "/");
        Set<File> result = new LinkedHashSet<File>(8);
        doRetrieveMatchingFiles(fullPattern, rootDir, result);
        return result;
    }

    /**
     * Recursively retrieve files that match the given pattern, adding them to the given result
     * list.
     *
     * @param fullPattern
     *            the pattern to match against, with prepended root directory path
     * @param dir
     *            the current directory
     * @param result
     *            the Set of matching File instances to add to
     * @throws IOException
     *             if directory contents could not be retrieved
     */
    private void doRetrieveMatchingFiles(String fullPattern, File dir, Set<File> result)
            throws IOException {
        File[] dirContents = dir.listFiles();
        if (dirContents == null) {
            System.out.println("Could not retrieve contents of directory [" + dir.getAbsolutePath()
                    + "]");
            return;
        }
        for (File content : dirContents) {
            String currPath = StringUtils.replace(content.getAbsolutePath(), File.separator, "/");
            if (content.isDirectory() && pathMatcher.matchStart(fullPattern, currPath + "/")) {
                if (!content.canRead()) {
                    System.out.println("Skipping subdirectory [" + dir.getAbsolutePath()
                            + "] because the application is not allowed to read the directory");
                } else {
                    doRetrieveMatchingFiles(fullPattern, content, result);
                }
            }
            if (pathMatcher.match(fullPattern, currPath)) {
                result.add(content);
            }
        }
    }

    private void addAllClassLoaderJarRoots(ClassLoader classLoader, Set<Resource> result) {
        if (classLoader instanceof URLClassLoader) {
            try {
                for (URL url : ((URLClassLoader) classLoader).getURLs()) {
                    if (ResourceUtils.isJarFileURL(url)) {
                        try {
                            UrlResource jarResource = new UrlResource(ResourceUtils.JAR_URL_PREFIX
                                    + url.toString() + ResourceUtils.JAR_URL_SEPARATOR);
                            if (jarResource.exists()) {
                                result.add(jarResource);
                            }
                        } catch (MalformedURLException ex) {
                            System.out.println("Cannot search for matching files underneath ["
                                    + url
                                    + "] because it cannot be converted to a valid 'jar:' URL: "
                                    + ex.getMessage());

                        }
                    }
                }
            } catch (Exception ex) {
                System.out.println("Cannot introspect jar files since ClassLoader [" + classLoader
                        + "] does not support 'getURLs()': " + ex);
            }
        }
        if (classLoader != null) {
            try {
                addAllClassLoaderJarRoots(classLoader.getParent(), result);
            } catch (Exception ex) {
                System.out.println("Cannot introspect jar files in parent ClassLoader since ["
                        + classLoader + "] does not support 'getParent()': " + ex);
            }
        }
    }

}
