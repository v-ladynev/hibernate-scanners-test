package com.github.ladynev.scanners;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
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

    private Collection<URL> getUrls(ClassLoader providedLoader, String... packagesToSacen) {
        List<URL> result = new ArrayList<URL>();
        ClassLoader[] loaders = classLoaders(providedLoader);

        for (String packageToScan : packagesToSacen) {
            forResource(ClassUtils.packageAsResourcePath(packageToScan), result, loaders);
        }

        if (result.isEmpty()) {
            for (ClassLoader loader : loaders) {
                forClassLoader(loader, result);
            }
        }

        return distinctUrls(result);
    }

    private static void forResource(String resourceName, List<URL> result, ClassLoader... loaders) {
        for (ClassLoader loader : loaders) {
            try {
                getUrls(resourceName, loader, result);
            } catch (IOException ignore) {

            }
        }
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

    private static void forClassLoader(ClassLoader loader, List<URL> result) {
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                if (urls != null) {
                    result.addAll(Arrays.asList(urls));
                }
            }
            loader = loader.getParent();
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
