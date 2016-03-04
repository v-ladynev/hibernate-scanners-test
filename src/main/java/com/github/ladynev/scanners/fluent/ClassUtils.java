package com.github.ladynev.scanners.fluent;

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

}
