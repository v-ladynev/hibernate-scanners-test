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

    private final Set<File> scannedUris = new HashSet<File>();

    private final Set<String> classResources = new HashSet<String>();

    private final String[] packagesToScan;

    private Class<? extends Annotation> annotation;

    private ClassLoader[] loaders;

    private FluentEntityScanner(String[] packagesToScan) {
        this.packagesToScan = packagesToScan;
    }

    /**
     *
     * @param packagesToScan
     *            one or more Java package names
     */
    public static FluentEntityScanner createForPackages(String... packages) {
        return new FluentEntityScanner(packages);
    }

    public FluentEntityScanner usingLoaders(ClassLoader... loaders) throws IOException {
        this.loaders = CollectionUtils.correctToNull(loaders);
        return this;
    }

    /**
     * Perform scanning for entity classes.
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
        List<ClassLoader> correctedLoaders = loaders == null ? ClassUtils.defaultClassLoaders()
                : Arrays.asList(loaders);
        Set<UrlWrapper> urls = UrlExtractor.createForPackages(packagesToScan)
                .usingLoaders(correctedLoaders).extract();

        for (UrlWrapper url : urls) {
            scan(url);
        }

        return result;
    }

    private final void scan(UrlWrapper url) throws IOException {
        if (url.isFile()) {
            scan(url.getFile(), url.getLoader());
        } else {
            scanJar(url.getJarFile(), url.getLoader());
        }
    }

    private final void scan(File file, ClassLoader loader) throws IOException {
        // scan each file once independent of the classloader
        if (scannedUris.add(file.getCanonicalFile())) {
            scanFrom(file, loader);
        }
    }

    private void scanFrom(File file, ClassLoader loader) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            scanDirectory(loader, file);
        } else {
            scanJar(createJarFile(file), loader);
        }
    }

    private static JarFile createJarFile(File file) {
        try {
            return new JarFile(file);
        } catch (IOException e) {
            // Not a jar file
            return null;
        }
    }

    private void scanJar(JarFile jarFile, ClassLoader loader) throws IOException {
        if (jarFile == null) {
            return;
        }

        try {
            for (File path : getClassPathFromManifest(jarFile)) {
                scan(path, loader);
            }
            scanJarFile(jarFile, loader);
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

    private static Set<File> getClassPathFromManifest(JarFile jarFile) throws IOException {
        Manifest manifest = jarFile.getManifest();

        if (manifest == null) {
            return Collections.emptySet();
        }

        Set<File> result = new HashSet<File>();
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
            if (ClassUtils.isFile(url)) {
                result.add(new File(url.getFile()));
            }
        }

        return result;
    }

    private void addClass(String classResource, ClassLoader loader) {
        if (!classResources.add(classResource)) {
            return;
        }

        for (String packageToScan : packagesToScan) {
            String prefix = ClassUtils.packageAsResourcePath(packageToScan);
            if (classResource.startsWith(prefix)) {
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
