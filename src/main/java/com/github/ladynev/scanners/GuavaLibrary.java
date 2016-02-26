package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import com.github.ladynev.scanners.util.ScannerAdapter;
import com.google.common.reflect.ClassPath;

/**
 * https://github.com/google/guava
 *
 * @author V.Ladynev
 */
public class GuavaLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (final ClassPath.ClassInfo info : ClassPath.from(getLoader()).getAllClasses()) {
            if (info.getName().startsWith(packageToScan)) {
                Class<?> clazz = info.load();
                if (isAnnotationPresent(clazz)) {
                    result.add(clazz);
                }
            }
        }

        return result;
    }

}
