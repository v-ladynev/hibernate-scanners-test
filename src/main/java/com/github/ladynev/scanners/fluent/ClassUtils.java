package com.github.ladynev.scanners.fluent;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.List;
import java.util.jar.JarFile;

/**
 *
 * @author V.Ladynev
 */
public final class ClassUtils {

    private static final char PACKAGE_SEPARATOR = '.';

    private static final char PATH_SEPARATOR = '/';

    private static final String CLASS_FILE_NAME_EXTENSION = ".class";

    private static final String URL_PROTOCOL_FILE = "file";

    private ClassUtils() {

    }

    public static Class<?> classForName(String className, ClassLoader loader) {
        try {
            return Class.forName(className, true, loader);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static List<String> packagesAsResourcePath(List<String> packageNames) {
        List<String> result = CollectionUtils.newArrayListWithCapacity(CollectionUtils
                .size(packageNames));

        for (String packageName : packageNames) {
            result.add(packageAsResourcePath(packageName));
        }

        return result;
    }

    public static String packageAsResourcePath(String packageName) {
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    public static String getClassNameFromPath(String classFilePath) {
        int classNameEnd = classFilePath.length() - CLASS_FILE_NAME_EXTENSION.length();
        return classFilePath.substring(0, classNameEnd).replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    public static String toDescriptor(Class<? extends Annotation> annotation) {
        return "L" + packageAsResourcePath(annotation.getName()) + ";";
    }

    public static boolean isFile(URL url) {
        return url != null && url.getProtocol().equals(URL_PROTOCOL_FILE);
    }

    public static JarFile createJarFile(File file) {
        try {
            return new JarFile(file);
        } catch (IOException ignore) {
            // Not a jar file
            return null;
        }
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
