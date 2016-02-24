package com.github.ladynev.scanners.util;

/**
 *
 * @author V.Ladynev
 */
public final class ClassUtils {

    private ClassUtils() {

    }

    public static Class<?> toClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

}
