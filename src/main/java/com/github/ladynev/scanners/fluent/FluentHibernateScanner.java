package com.github.ladynev.scanners.fluent;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.annotation.Nullable;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

/**
 *
 * @author V.Ladynev
 */
public final class FluentHibernateScanner {

    private static final Splitter CLASS_PATH_ATTRIBUTE_SEPARATOR = Splitter.on(" ")
            .omitEmptyStrings();

    private final Set<File> scannedUris = Sets.newHashSet();

    private final SetMultimap<ClassLoader, String> resources = MultimapBuilder.hashKeys()
            .linkedHashSetValues().build();

    private final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private final String[] packagesToScan;

    private final Class<? extends Annotation> annotation;

    private FluentHibernateScanner(Class<? extends Annotation> annotation, String[] packagesToScan) {
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
        return new FluentHibernateScanner(annotation, packagesToScan).scan();
    }

    private List<Class<?>> scan() throws IOException {

        for (Map.Entry<File, ClassLoader> entry : getClassPathEntries(loader).entrySet()) {
            scan(entry.getKey(), entry.getValue());
        }

        return getClasses();
    }

    private List<Class<?>> getClasses() {
        List<Class<?>> result = new ArrayList<Class<?>>();

        for (Map.Entry<ClassLoader, String> entry : resources.entries()) {
            for (String packageToScan : packagesToScan) {
                String prefix = ClassUtils.packageAsResourcePath(packageToScan);
                String className = entry.getValue();
                if (className.startsWith(prefix)) {
                    Class<?> clazz = ClassUtils.classForName(
                            ClassUtils.getClassNameFromPath(entry.getValue()), entry.getKey());
                    if (clazz.isAnnotationPresent(annotation)) {
                        result.add(clazz);
                    }
                }
            }
        }

        return result;
    }

    private static ImmutableMap<File, ClassLoader> getClassPathEntries(ClassLoader classloader) {
        LinkedHashMap<File, ClassLoader> entries = Maps.newLinkedHashMap();
        // Search parent first, since it's the order ClassLoader#loadClass() uses.
        ClassLoader parent = classloader.getParent();
        if (parent != null) {
            entries.putAll(getClassPathEntries(parent));
        }
        if (classloader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classloader;
            for (URL entry : urlClassLoader.getURLs()) {
                if (entry.getProtocol().equals("file")) {
                    File file = new File(entry.getFile());
                    if (!entries.containsKey(file)) {
                        entries.put(file, classloader);
                    }
                }
            }
        }
        return ImmutableMap.copyOf(entries);
    }

    private final void scan(File file, ClassLoader classloader) throws IOException {
        if (scannedUris.add(file.getCanonicalFile())) {
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
            resources.get(classloader).add(entry.getName());
        }
    }

    private void scanDirectory(ClassLoader classloader, File directory) throws IOException {
        scanDirectory(directory, classloader, "");
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
                    resources.get(classloader).add(resourceName);
                }
            }
        }
    }

    private static ImmutableSet<File> getClassPathFromManifest(File jarFile,
            @Nullable Manifest manifest) {
        if (manifest == null) {
            return ImmutableSet.of();
        }
        ImmutableSet.Builder<File> builder = ImmutableSet.builder();
        String classpathAttribute = manifest.getMainAttributes().getValue(
                Attributes.Name.CLASS_PATH.toString());
        if (classpathAttribute != null) {
            for (String path : CLASS_PATH_ATTRIBUTE_SEPARATOR.split(classpathAttribute)) {
                URL url;
                try {
                    url = getClassPathEntry(jarFile, path);
                } catch (MalformedURLException e) {
                    // Ignore bad entry
                    continue;
                }
                if (url.getProtocol().equals("file")) {
                    builder.add(new File(url.getFile()));
                }
            }
        }
        return builder.build();
    }

    private static URL getClassPathEntry(File jarFile, String path) throws MalformedURLException {
        return new URL(jarFile.toURI().toURL(), path);
    }

}
