package com.github.ladynev.scanners.fluent;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 *
 * @author V.Ladynev
 */
public final class UrlExtractor {

    private final String[] packages;

    private final Map<UrlWrapper, ClassLoader> result = CollectionUtils.newHashMap();

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

    public Map<UrlWrapper, ClassLoader> extract() {
        for (String p : packages) {
            forPackage(p);
        }

        if (result.isEmpty()) {
            for (ClassLoader loader : loaders) {
                forClassLoader(loader);
            }
        }

        return result;
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
        Enumeration<URL> urls = loader.getResources(resourceName);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            int index = url.toExternalForm().lastIndexOf(resourceName);
            addUrl(index == -1 ? url : new URL(url.toExternalForm().substring(0, index)), loader);
        }
    }

    private void forClassLoader(ClassLoader loader) {
        while (loader != null) {
            if (loader instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader) loader).getURLs();
                addUrls(urls, loader);
            }
            loader = loader.getParent();
        }
    }

    private void addUrls(URL[] urls, ClassLoader loader) {
        if (urls == null) {
            return;
        }

        for (URL url : urls) {
            addUrl(url, loader);
        }
    }

    private void addUrl(URL url, ClassLoader loader) {
        UrlWrapper urlWrapper = new UrlWrapper(url);
        if (!result.containsKey(urlWrapper)) {
            result.put(urlWrapper, loader);
        }
    }

}
