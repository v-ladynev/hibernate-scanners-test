package com.github.ladynev.scanners;

import java.io.IOException;
import java.util.List;

import javax.persistence.Entity;

import com.google.common.reflect.ClassPath;

/**
 *
 * @author V.Ladynev
 */
public class GuavaScanner implements IScanner {

    private final ClassLoader loader;

    public GuavaScanner() {
        loader = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public List<Class<?>> scan(String packageToScan, List<Class<?>> result) throws IOException {
        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getAllClasses()) {
            if (info.getName().startsWith(packageToScan)) {
                Class<?> clazz = info.load();
                if (clazz.isAnnotationPresent(Entity.class)) {
                    result.add(clazz);
                }
            }
        }

        return result;
    }

}
