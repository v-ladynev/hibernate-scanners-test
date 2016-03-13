package com.github.ladynev.scanners.fluent;

import java.io.File;
import java.io.IOException;
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
public class FluentClasspathScanner {

    private final List<Class<?>> result = new ArrayList<Class<?>>();

    private final Set<UrlWrapper> scanned = CollectionUtils.newHashSet();

    private final Set<String> scannedResources = new HashSet<String>();

    private List<String> resourcesToScan;

    private List<ClassLoader> loaders;

    private final IClassAcceptor acceptor;

    public FluentClasspathScanner(IClassAcceptor acceptor) {
        this.acceptor = acceptor;
    }

    public void setPackagesToScan(String[] packagesToScan) {
        this.resourcesToScan = ClassUtils.packagesAsResourcePath(Arrays.asList(packagesToScan));
    }

    public void setLoaders(List<ClassLoader> loaders) {
        this.loaders = loaders;
    }

    public List<Class<?>> scan() throws Exception {
        for (UrlWrapper url : getUrls()) {
            scan(url);
        }

        return result;
    }

    private Set<UrlWrapper> getUrls() {
        List<ClassLoader> correctedLoaders = CollectionUtils.isEmpty(loaders) ? ClassUtils
                .defaultClassLoaders() : loaders;
                return UrlExtractor.createForResources(resourcesToScan).usingLoaders(correctedLoaders)
                        .extract();
    }

    private void scan(UrlWrapper url) throws Exception {
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

    private void scanFile(UrlWrapper url) throws Exception {
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

    private void scanJar(UrlWrapper url) throws Exception {
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

    private void scanJarFile(JarFile file, ClassLoader loader) throws Exception {
        Enumeration<JarEntry> entries = file.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.isDirectory() || entry.getName().equals(JarFile.MANIFEST_NAME)) {
                continue;
            }

            addClass(entry.getName(), loader);
        }
    }

    private void scanDirectory(ClassLoader loader, File directory) throws Exception {
        scanDirectory(directory, loader, StringUtils.EMPTY);
    }

    private void scanDirectory(File directory, ClassLoader classloader, String packagePrefix)
            throws Exception {
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

    private void addClass(String classResource, ClassLoader loader) throws Exception {
        if (!scannedResources.add(classResource)) {
            return;
        }

        if (acceptor.accept(classResource, loader)) {
            Class<?> clazz = ClassUtils.classForName(
                    ClassUtils.getClassNameFromPath(classResource), loader);
            result.add(clazz);
        }
    }

    private static URL getClassPathEntry(JarFile jarFile, String path) throws MalformedURLException {
        return new URL(new File(jarFile.getName()).toURI().toURL(), path);
    }

    public interface IClassAcceptor {

        boolean accept(String classResource, ClassLoader loader) throws Exception;
    }

}
