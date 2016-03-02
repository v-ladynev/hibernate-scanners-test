package com.github.ladynev.scanners.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

/**
 *
 * @author V.Ladynev
 */
public abstract class ScannerAdapter implements IScanner {

    private ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private Class<? extends Annotation> annotation = Entity.class;

    private ClassLoader prevContextClassLoader;

    private boolean tuned;

    @Override
    public void tune(ClassLoader loader, Class<? extends Annotation> annotation) {
        this.loader = loader;
        this.annotation = annotation;
        tuned = true;
    }

    protected ClassLoader getLoader() {
        return loader;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    protected boolean isAnnotationPresent(Class<?> clazz) {
        return clazz.isAnnotationPresent(annotation);
    }

    protected boolean isTuned() {
        return tuned;
    }

    protected void replaceContextClassLoader() {
        prevContextClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
    }

    protected void backContextClassLoader() {
        Thread.currentThread().setContextClassLoader(prevContextClassLoader);
    }

    @Override
    public List<Class<?>> scan(String... packagesToScan) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (String packageToScan : packagesToScan) {
            result.addAll(scan(packageToScan));
        }

        return result;
    }

}
