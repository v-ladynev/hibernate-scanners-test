package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import pro.ddopson.ClassEnumerator;

/**
 *
 * @author V.Ladynev
 */
public class ClassEnumeratorScanner implements IScanner {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();

        List<Class<?>> discoveredClasses = ClassEnumerator.getClassesForPackage(packageToScan);
        for (Class<?> clazz : discoveredClasses) {
            if (clazz.isAnnotationPresent(Entity.class)) {
                result.add(clazz);
            }
        }

        return result;
    }

}
