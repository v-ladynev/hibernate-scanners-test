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

import sun.net.www.protocol.file.FileURLConnection;

import com.github.ladynev.scanners.util.ClassUtils;
import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * http://stackoverflow.com/a/22462785/3405171
 *
 * @author V.Ladynev
 */
public class CustomScanner extends ScannerAdapter {

    ArrayList<Class<?>> result = new ArrayList<Class<?>>();

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        return getClassesForPackage(packageToScan);
    }

    private ArrayList<Class<?>> getClassesForPackage(String pckgname) throws Exception {

        final Enumeration<URL> resources = getLoader().getResources(pckgname.replace('.', '/'));

        for (URL url = null; resources.hasMoreElements() && (url = resources.nextElement()) != null;) {
            URLConnection connection = url.openConnection();

            if (connection instanceof JarURLConnection) {
                checkJarFile((JarURLConnection) connection, pckgname);
            } else if (connection instanceof FileURLConnection) {
                checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")), pckgname);
            }
        }

        return result;
    }

    private void checkDirectory(File directory, String pckgname) throws Exception {
        File tmpDirectory;

        if (directory.exists() && directory.isDirectory()) {
            final String[] files = directory.list();

            for (final String file : files) {
                if (file.endsWith(".class")) {
                    try {
                        addClass(pckgname + '.' + file.substring(0, file.length() - 6));
                    } catch (final NoClassDefFoundError e) {
                        // do nothing. this class hasn't been found by the
                        // loader, and we don't care.
                    }
                } else if ((tmpDirectory = new File(directory, file)).isDirectory()) {
                    checkDirectory(tmpDirectory, pckgname + "." + file);
                }
            }
        }
    }

    private void checkJarFile(JarURLConnection connection, String pckgname) throws Exception {
        final JarFile jarFile = connection.getJarFile();
        final Enumeration<JarEntry> entries = jarFile.entries();
        String name;

        for (JarEntry jarEntry = null; entries.hasMoreElements()
                && (jarEntry = entries.nextElement()) != null;) {
            name = jarEntry.getName();

            if (name.contains(".class")) {
                name = name.substring(0, name.length() - 6).replace('/', '.');

                if (name.contains(pckgname)) {
                    addClass(name);
                }
            }
        }

    }

    private void addClass(String className) {
        Class<?> clazz = ClassUtils.toClass(className, getLoader());
        if (isAnnotationPresent(clazz)) {
            result.add(clazz);
        }
    }

}
