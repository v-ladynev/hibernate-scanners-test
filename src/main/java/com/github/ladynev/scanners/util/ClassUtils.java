package com.github.ladynev.scanners.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author V.Ladynev
 */
public final class ClassUtils {

    private static final char PACKAGE_SEPARATOR = '.';

    private static final char PATH_SEPARATOR = '/';

    private static final String PATH_SEPARATOR_AS_STRING = String.valueOf(PATH_SEPARATOR);

    public static final String CLASS_FILE_SUFFIX = ".class";

    private ClassUtils() {

    }

    public static Class<?> toClass(String className, ClassLoader loader) {
        try {
            return Class.forName(className, true, loader);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static URLClassLoader createClassLoader(ClassLoader parent, URL... url)
            throws MalformedURLException {
        return new URLClassLoader(url, parent);
    }

    public static URL urlForJar(String jarName) {
        URLClassLoader loader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        for (URL url : loader.getURLs()) {
            if (url.getPath().endsWith(jarName)) {
                return url;
            }
        }

        return null;
    }

    public static String classAsResource(String className) {
        return className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR) + CLASS_FILE_SUFFIX;
    }

    public static String resourcePathFromRoot(String resourcePath) {
        return resourcePath.startsWith(PATH_SEPARATOR_AS_STRING) ? resourcePath : PATH_SEPARATOR
                + resourcePath;
    }

}
