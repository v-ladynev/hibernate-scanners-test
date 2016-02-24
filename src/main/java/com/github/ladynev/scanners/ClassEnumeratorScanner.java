package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import pro.ddopson.ClassEnumerator;

/**
 *
 * @author V.Ladynev
 */
public class ClassEnumeratorScanner implements IScanner {

    @Override
    public List<Class<?>> scan(String packageToScan, IAccept accept) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();

        List<Class<?>> discoveredClasses = ClassEnumerator.getClassesForPackage(packageToScan);
        for (Class<?> clazz : discoveredClasses) {
            if (accept.clazz(clazz)) {
                result.add(clazz);
            }
        }

        return result;
    }

}
