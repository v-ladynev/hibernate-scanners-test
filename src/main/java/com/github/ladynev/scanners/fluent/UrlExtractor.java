package com.github.ladynev.scanners.fluent;

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

/**
 *
 * @author V.Ladynev
 */
public final class UrlExtractor {

    private final String[] packages;

    private final List<URL> result = new ArrayList<URL>();

    private List<ClassLoader> loaders;

    private UrlExtractor(String... packages) {
        this.packages = packages;
    }

    public static UrlExtractor create(String... packages) {
        return new UrlExtractor(packages);
    }

    public UrlExtractor usingLoaders(List<ClassLoader> loaders) {
        this.loaders = loaders;
        return this;
    }

    public Collection<URL> extract() {
        for (String p : packages) {
            forPackage(p);
        }

        if (result.isEmpty()) {
            for (ClassLoader loader : loaders) {
                forClassLoader(loader);
            }
        }

        return distinct(result);
    }

    private static Collection<URL> distinct(Collection<URL> urls) {
        Map<String, URL> result = new HashMap<String, URL>(urls.size());
        for (URL url : urls) {
            result.put(url.toExternalForm(), url);
        }
        return result.values();
    }

    private void forPackage(String packageToScan) {
        for (ClassLoader loader : loaders) {
            try {
                getUrls(ClassUtils.packageAsResourcePath(packageToScan), loader);
            } catch (IOException ignore) {

            }
        }
    }

    private void getUrls(String resourceName, ClassLoader loader) throws IOException {
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

    private void forClassLoader(ClassLoader loader) {
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

}
