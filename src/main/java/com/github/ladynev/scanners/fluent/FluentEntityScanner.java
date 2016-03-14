package com.github.ladynev.scanners.fluent;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;

/**
 *
 * @author V.Ladynev
 */
public final class FluentEntityScanner {

    private AnnotationChecker checker;

    private List<ClassLoader> loaders;

    private final List<String> packagesToScan;

    private FluentEntityScanner(String[] packagesToScan) {
        this.packagesToScan = packagesToScan == null ? Collections.<String> emptyList() : Arrays
                .asList(CollectionUtils.correctOneNullToEmpty(packagesToScan));
    }

    /**
     * Scan packages for the @Entity annotation.
     *
     *
     * @param packages
     *            one or more Java package names
     *
     * @throws Exception
     *             if scanning fails for any reason
     *
     * @return entity classes
     */
    public static List<Class<?>> scanPackages(String... packages) throws Exception {
        return scanPackages(packages, null, Entity.class);
    }

    static List<Class<?>> scanPackages(String[] packages, List<ClassLoader> loaders,
            Class<? extends Annotation> annotation) throws Exception {
        FluentEntityScanner scanner = new FluentEntityScanner(packages);
        scanner.loaders = loaders;
        return scanner.scan(annotation);
    }

    private List<Class<?>> scan(Class<? extends Annotation> annotation) throws Exception {
        checker = new AnnotationChecker(annotation);

        FluentClasspathScanner scanner = new FluentClasspathScanner(
                new FluentClasspathScanner.IClassAcceptor() {
                    @Override
                    public boolean accept(String classResource, ClassLoader loader)
                            throws Exception {
                        return checker.hasAnnotation(loader.getResourceAsStream(classResource));
                    }
                });

        scanner.setPackagesToScan(packagesToScan);
        scanner.setLoaders(loaders);

        return scanner.scan();
    }

}
