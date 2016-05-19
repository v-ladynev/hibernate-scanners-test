package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 * https://github.com/ronmamo/reflections
 *
 * @author V.Ladynev
 */
public class ReflectionsCopyLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String... packagesToScan) throws Exception {
        Reflections reflections = new Reflections(packagesToScan, getLoader());

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(getAnnotation());

        return new ArrayList<Class<?>>(annotated);
    }

}
