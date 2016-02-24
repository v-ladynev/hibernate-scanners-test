package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.ClassPath;

/**
 * https://github.com/google/guava
 *
 * @author V.Ladynev
 */
public class GuavaLibrary implements IScanner {

    private final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    @Override
    public List<Class<?>> scan(String packageToScan, IAccept accept) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (final ClassPath.ClassInfo info : ClassPath.from(loader).getAllClasses()) {
            if (info.getName().startsWith(packageToScan)) {
                Class<?> clazz = info.load();
                if (accept.clazz(clazz)) {
                    result.add(clazz);
                }
            }
        }

        return result;
    }

}
