package com.github.ladynev.scanners;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.github.ladynev.scanners.fluent.ClassUtils;
import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 * https://github.com/ronmamo/reflections
 *
 * @author V.Ladynev
 */
public class ReflectionsCopyLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String... packagesToScan) throws Exception {
        // ArrayList<URL> list = Collections.list(getLoader().getResources("com/github/ladynev/"));
        // System.out.println(list);

        // System.out.println(Arrays.asList(((URLClassLoader) getLoader()).getURLs()));

        /*
        List<URL> list = Arrays.asList(((URLClassLoader) getLoader()).getURLs());

        URL url = list.get(0);
        JarURLConnection urlcon = (JarURLConnection) url.openConnection();
        JarFile jar = urlcon.getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement().getName();
            System.out.println(entry);
        }
         */

        /*
        Reflections reflections = isTuned() ? new Reflections(packagesToScan, getLoader())
                : new Reflections(packagesToScan);
         */

        // /System.out.println(ScannersUtils.urlForJar("scanners-test.jar"));

        System.out.println(getUrls(null, packagesToScan));

        replaceContextClassLoader();

        Reflections reflections = new Reflections(packagesToScan);

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(getAnnotation());

        backContextClassLoader();

        return new ArrayList<Class<?>>(annotated);
    }

    private Collection<URL> getUrls(ClassLoader loader, String... packagesToSacen) {
        List<URL> result = new ArrayList<URL>();
        ClassLoader[] loaders = classLoaders(loader);

        for (String packageToScan : packagesToSacen) {
            result.addAll(forPackage(packageToScan, loaders));
        }

        return result;
    }

    private static Collection<URL> forPackage(String packageName, ClassLoader... loaders) {
        return forResource(ClassUtils.packageAsResourcePath(packageName), loaders);
    }

    private static Collection<URL> forResource(String resourceName, ClassLoader... loaders) {
        final List<URL> result = new ArrayList<URL>();
        for (ClassLoader classLoader : loaders) {
            try {
                final Enumeration<URL> urls = classLoader.getResources(resourceName);
                while (urls.hasMoreElements()) {
                    final URL url = urls.nextElement();
                    int index = url.toExternalForm().lastIndexOf(resourceName);
                    if (index != -1) {
                        result.add(new URL(url.toExternalForm().substring(0, index)));
                    } else {
                        result.add(url);
                    }
                }
            } catch (IOException e) {
                // slalow exception
            }
        }
        return distinctUrls(result);
    }

    private static ClassLoader[] classLoaders(ClassLoader loader) {
        if (loader != null) {
            return new ClassLoader[] { loader };
        } else {
            ClassLoader contextClassLoader = contextClassLoader(), staticClassLoader = staticClassLoader();
            return contextClassLoader != null ? staticClassLoader != null
                    && contextClassLoader != staticClassLoader ? new ClassLoader[] {
                    contextClassLoader, staticClassLoader }
                    : new ClassLoader[] { contextClassLoader } : new ClassLoader[] {};

        }
    }

    private static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private static ClassLoader staticClassLoader() {
        return ReflectionsCopyLibrary.class.getClassLoader();
    }

    private static Collection<URL> distinctUrls(Collection<URL> urls) {
        Map<String, URL> distinct = new HashMap<String, URL>(urls.size());
        for (URL url : urls) {
            distinct.put(url.toExternalForm(), url);
        }
        return distinct.values();
    }

}
