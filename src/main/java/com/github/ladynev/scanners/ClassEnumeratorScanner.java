package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import pro.ddopson.ClassEnumerator;

import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class ClassEnumeratorScanner extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();

        ClassEnumerator enumerator = new ClassEnumerator();
        if (isTuned()) {
            enumerator.setLoader(getLoader());
        }

        List<Class<?>> discoveredClasses = enumerator.getClassesForPackage(packageToScan);
        for (Class<?> clazz : discoveredClasses) {
            if (isAnnotationPresent(clazz)) {
                result.add(clazz);
            }
        }

        return result;
    }

}
