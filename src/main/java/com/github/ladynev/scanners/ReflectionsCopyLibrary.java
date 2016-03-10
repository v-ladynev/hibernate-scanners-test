package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.github.ladynev.scanners.fluent.ClassUtils;
import com.github.ladynev.scanners.fluent.UrlExtractor;
import com.github.ladynev.scanners.fluent.UrlWrapper;
import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 * https://github.com/ronmamo/reflections
 *
 * @author V.Ladynev
 */
public class ReflectionsCopyLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String... packagesToScan) throws Exception {
        System.out.println(getUrls(isTuned() ? getLoader() : null, packagesToScan));

        Reflections reflections = new Reflections(packagesToScan, getLoader());

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(getAnnotation());

        return new ArrayList<Class<?>>(annotated);
    }

    private Collection<UrlWrapper> getUrls(ClassLoader loader, String[] packagesToScan) {
        List<ClassLoader> loaders = loader == null ? ClassUtils.defaultClassLoaders() : Arrays
                .asList(loader);

        return UrlExtractor.createForPackages(packagesToScan).usingLoaders(loaders).extract();
    }

}
