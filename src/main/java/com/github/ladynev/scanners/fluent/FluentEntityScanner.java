package com.github.ladynev.scanners.fluent;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 *
 * @author V.Ladynev
 */
public final class FluentEntityScanner {

    private final List<Class<?>> result = new ArrayList<Class<?>>();

    private final Set<UrlWrapper> scanned = CollectionUtils.newHashSet();

    private final Set<String> classResources = new HashSet<String>();

    private final List<String> resourcesToScan;

    private Class<? extends Annotation> annotation;

    private ClassLoader[] loaders;

    private FluentEntityScanner(String[] packagesToScan) {
        this.resourcesToScan = ClassUtils.packagesAsResourcePath(Arrays.asList(packagesToScan));
    }

    /**
     *
     * @param packagesToScan
     *            one or more Java package names
     */
    public static FluentEntityScanner createForPackages(String... packages) {
        return new FluentEntityScanner(CollectionUtils.correctToEmpty(packages));
    }

    public FluentEntityScanner usingLoaders(ClassLoader... loaders) throws IOException {
        this.loaders = CollectionUtils.correctToEmpty(loaders);
        return this;
    }

    /**
     * Perform scanning for classes with an annotation.
     *
     * @param annotation
     *            an annotation to find
     *
     * @throws IOException
     *             if scanning fails for any reason
     *
     * @return entity classes
     */
    public List<Class<?>> scan(Class<? extends Annotation> annotation) throws IOException {
        this.annotation = annotation;
        return scan();
    }

    private List<Class<?>> scan() throws IOException {
        List<ClassLoader> correctedLoaders = CollectionUtils.isEmpty(loaders) ? ClassUtils
                .defaultClassLoaders() : Arrays.asList(loaders);
                Set<UrlWrapper> urls = UrlExtractor.createForResources(resourcesToScan)
                        .usingLoaders(correctedLoaders).extract();

                for (UrlWrapper url : urls) {
                    scan(url);
                }

                return result;
    }

    private void scan(UrlWrapper url) throws IOException {
        // scan each url once independent of the classloader
        if (!scanned.add(url)) {
            return;
        }

        if (url.isFile()) {
            scanFile(url);
        } else {
            scanJar(url);
        }
    }

    private void scanFile(UrlWrapper url) throws IOException {
        File file = url.getFile();

        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            scanDirectory(url.getLoader(), file);
        } else {
            scanJar(url);
        }
    }

    private void scanJar(UrlWrapper url) throws IOException {
        JarFile jarFile = url.getJarFile();

        if (jarFile == null) {
            return;
        }

        try {
            for (UrlWrapper urlFromManifest : getClassPathFromManifest(jarFile, url.getLoader())) {
                scan(urlFromManifest);
            }
            scanJarFile(jarFile, url.getLoader());
        } finally {
            try {
                jarFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void scanJarFile(JarFile file, ClassLoader loader) {
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
                continue;
            }

            addClass(entry.getName(), loader);
        }
    }

    private void scanDirectory(ClassLoader loader, File directory) throws IOException {
        scanDirectory(directory, loader, StringUtils.EMPTY);
    }

    private void scanDirectory(File directory, ClassLoader classloader, String packagePrefix)
            throws IOException {
        File[] files = directory.listFiles();
        if (files == null) {
            // IO error, just skip the directory
            return;
        }
        for (File f : files) {
            String name = f.getName();
            if (f.isDirectory()) {
                scanDirectory(f, classloader, packagePrefix + name + "/");
            } else {
                String resourceName = packagePrefix + name;
                if (!resourceName.equals(JarFile.MANIFEST_NAME)) {
                    addClass(resourceName, classloader);
                }
            }
        }
    }

    private static Set<UrlWrapper> getClassPathFromManifest(JarFile jarFile, ClassLoader loader)
            throws IOException {
        Manifest manifest = jarFile.getManifest();

        if (manifest == null) {
            return Collections.emptySet();
        }

        Set<UrlWrapper> result = CollectionUtils.newHashSet();
        String classpathAttribute = manifest.getMainAttributes().getValue(
                Attributes.Name.CLASS_PATH.toString());
        if (classpathAttribute == null) {
            return result;
        }

        for (String path : StringUtils.splitBySpace(classpathAttribute)) {
            URL url;
            try {
                url = getClassPathEntry(jarFile, path);
            } catch (MalformedURLException e) {
                // Ignore bad entry
                continue;
            }
            result.add(new UrlWrapper(url, loader));
        }

        return result;
    }

    private void addClass(String classResource, ClassLoader loader) {
        if (!classResources.add(classResource)) {
            return;
        }

        for (String resourceToScan : resourcesToScan) {
            if (classResource.startsWith(resourceToScan)) {
                Class<?> clazz = ClassUtils.classForName(
                        ClassUtils.getClassNameFromPath(classResource), loader);
                if (clazz.isAnnotationPresent(annotation)) {
                    result.add(clazz);
                    return;
                }
            }
        }
    }

    private static URL getClassPathEntry(JarFile jarFile, String path) throws MalformedURLException {
        return new URL(new File(jarFile.getName()).toURI().toURL(), path);
    }

}
