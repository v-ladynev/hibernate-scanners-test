package com.github.ladynev.scanners.fluent;

import java.util.List;

/**
 *
 * @author V.Ladynev
 */
public final class ClassUtils {

    private static final char PACKAGE_SEPARATOR = '.';

    private static final char PATH_SEPARATOR = '/';

    private static final String CLASS_FILE_NAME_EXTENSION = ".class";

    private ClassUtils() {

    }

    public static Class<?> classForName(String className, ClassLoader loader) {
        try {
            return Class.forName(className, true, loader);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String packageAsResourcePath(String packageName) {
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    public static String getClassNameFromPath(String classFilePath) {
        int classNameEnd = classFilePath.length() - CLASS_FILE_NAME_EXTENSION.length();
        return classFilePath.substring(0, classNameEnd).replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    public static List<ClassLoader> defaultClassLoaders() {
        List<ClassLoader> result = CollectionUtils.newArrayList();

        ClassLoader contextClassLoader = contextClassLoader();
        ClassLoader staticClassLoader = staticClassLoader();

        add(result, contextClassLoader);
        if (contextClassLoader != staticClassLoader) {
            add(result, staticClassLoader);
        }

        return result;
    }

    private static void add(List<ClassLoader> result, ClassLoader loader) {
        if (loader != null) {
            result.add(loader);
        }
    }

    public static ClassLoader contextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader staticClassLoader() {
        return ClassUtils.class.getClassLoader();
    }

}
