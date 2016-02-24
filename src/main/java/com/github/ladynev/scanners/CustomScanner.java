package com.github.ladynev.scanners;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.persistence.Entity;

import sun.net.www.protocol.file.FileURLConnection;

/**
 *
 * http://stackoverflow.com/a/22462785/3405171
 *
 * @author V.Ladynev
 */
public class CustomScanner implements IScanner {

    private final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        return getClassesForPackage(packageToScan);
    }

    private ArrayList<Class<?>> getClassesForPackage(String pckgname) throws Exception {
        final ArrayList<Class<?>> result = new ArrayList<Class<?>>();

        final Enumeration<URL> resources = loader.getResources(pckgname.replace('.', '/'));

        for (URL url = null; resources.hasMoreElements() && (url = resources.nextElement()) != null;) {

            URLConnection connection = url.openConnection();

            if (connection instanceof JarURLConnection) {
                checkJarFile((JarURLConnection) connection, pckgname, result);
            } else if (connection instanceof FileURLConnection) {
                checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pckgname,
                        result);
            }
        }

        return result;
    }

    private void checkDirectory(File directory, String pckgname, ArrayList<Class<?>> result)
            throws Exception {
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files) {
                if (file.endsWith(".class")) {
                    try {

                        Class<?> clazz = Class.forName(pckgname + '.'
                                + file.substring(0, file.length() - 6));

                        if (check(clazz)) {
                            result.add(clazz);
                        }
                    } catch (final NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the
                        // loader, and we don't care.
                    }
                } else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
                    checkDirectory(tmpDirectory, pckgname + "." + file, result);
                }
            }
        }
    }

    private void checkJarFile(JarURLConnection connection, String pckgname,
            ArrayList<Class<?>> result) throws Exception {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry = null; entries.hasMoreElements()
                && (jarEntry = entries.nextElement()) != null;) {
            name = jarEntry.getName();

            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');

                if (name.contains(pckgname)) {
                    Class<?> clazz = Class.forName(name);
                    if (check(clazz)) {
                        result.add(clazz);
                    }
                }
            }
        }

    }

    private static boolean check(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class);
    }

}
