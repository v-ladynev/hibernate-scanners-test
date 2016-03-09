package com.github.ladynev.scanners.fluent;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private final String[] packagesToScan;

    private final Class<? extends Annotation> annotation;

    private FluentEntityScanner(Class<? extends Annotation> annotation, String[] packagesToScan) {
        this.annotation = annotation;
        this.packagesToScan = packagesToScan;
    }

    /**
     * Perform scanning for entity classes.
     *
     * @param annotation
     *            annotation to find
     * @param packagesToScan
     *            one or more Java package names
     *
     * @throws IOException
     *             if scanning fails for any reason
     *
     * @return entity classes
     */
    public static List<Class<?>> scanPackages(Class<? extends Annotation> annotation,
            String... packagesToScan) throws IOException {
        return new FluentEntityScanner(annotation, packagesToScan).scan();
    }

    private List<Class<?>> scan() throws IOException {

        for (Map.Entry<File, ClassLoader> entry : getClassPathEntries(loader).entrySet()) {
            scan(entry.getKey(), entry.getValue());
        }

        return result;
    }

    private static Map<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
        LinkedHashMap<File, ClassLoader> result = new LinkedHashMap<File, ClassLoader>();
        // Search parent first, since it's the order ClassLoader#loadClass() uses.
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            result.putAll(getClassPathEntries(parent));
        }
        if (classloader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classloader;
            for (URL entry : urlClassLoader.getURLs()) {
                if (entry.getProtocol().equals("file")) {
                    File file = new File(entry.getFile());
                    if (!result.containsKey(file)) {
                        result.put(file, classloader);
                    }
                }
            }
        }
        return result;
    }

    private final void scan(File file, ClassLoader classloader) throws IOException {
        // scan each file once independent of the classloader
        if (scannedUris.add(file.getCanonicalFile())) {
            // TODO don't scan jre
            scanFrom(file, classloader);
        }
    }

    private void scanFrom(File file, ClassLoader classloader) throws IOException {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            scanDirectory(classloader, file);
        } else {
            scanJar(file, classloader);
        }
    }

    private void scanJar(File file, ClassLoader classloader) throws IOException {
        JarFile jarFile;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            // Not a jar file
            return;
        }
        try {
            for (File path : getClassPathFromManifest(file, jarFile.getManifest())) {
                scan(path, classloader);
            }
            scanJarFile(classloader, jarFile);
        } finally {
            try {
                jarFile.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void scanJarFile(ClassLoader classloader, JarFile file) {
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
                continue;
            }

            addClass(entry.getName(), classloader);
        }
    }

    private void scanDirectory(ClassLoader classloader, File directory) throws IOException {
        scanDirectory(directory, classloader, StringUtils.EMPTY);
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

    private static Set<File> getClassPathFromManifest(File jarFile, Manifest manifest) {
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
            if (url.getProtocol().equals("file")) {
                result.add(new File(url.getFile()));
            }
        }

        return result;
    }

    private void addClass(String classResource, ClassLoader loader) {
        System.out.println(classResource);
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

    private static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
        return new URL(jarFile.toURI().toURL(), path);
    }

}
