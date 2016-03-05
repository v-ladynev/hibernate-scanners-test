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
        System.out.println(getUrls(getLoader(), packagesToScan));

        Reflections reflections = new Reflections(packagesToScan, getLoader());

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(getAnnotation());

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
        List<URL> result = new ArrayList<URL>();
        for (ClassLoader loader : loaders) {
            try {
                getUrls(resourceName, loader, result);
            } catch (IOException ignore) {

            }
        }
        return distinctUrls(result);
    }

    private static void getUrls(String resourceName, ClassLoader loader, List<URL> result)
            throws IOException {
        final Enumeration<URL> urls = loader.getResources(resourceName);
        while (urls.hasMoreElements()) {
            final URL url = urls.nextElement();
            int index = url.toExternalForm().lastIndexOf(resourceName);
            if (index != -1) {
                result.add(new URL(url.toExternalForm().substring(0, index)));
            } else {
                result.add(url);
            }
        }
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
