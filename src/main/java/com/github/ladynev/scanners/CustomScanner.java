package com.github.ladynev.scanners;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

/**
 *
 * http://stackoverflow.com/a/22462785/3405171
 *
 * @author V.Ladynev
 */
public class CustomScanner implements IScanner {

    private IAccept accept;

    @Override
    public List<Class<?>> scan(String packageToScan, IAccept accept) throws Exception {
        this.accept = accept;
        return getClassesForPackage(packageToScan);
    }

    /**
     * Attempts to list all the classes in the specified package as determined by the context class
     * loader
     *
     * @param pckgname
     *            the package name to search
     * @return a list of classes that exist within that package
     * @throws ClassNotFoundException
     *             if something went wrong
     */
    private ArrayList<Class<?>> getClassesForPackage(String pckgname) throws Exception {
        final ArrayList<Class<?>> result = new ArrayList<Class<?>>();

        try {
            final ClassLoader cld = Thread.currentThread().getContextClassLoader();

            if (cld == null) {
                throw new ClassNotFoundException("Can't get class loader.");
            }

            final Enumeration<URL> resources = cld.getResources(pckgname.replace('.', '/'));
            URLConnection connection;

            for (URL url = null; resources.hasMoreElements()
                    && (url = resources.nextElement()) != null;) {
                try {
                    connection = url.openConnection();

                    if (connection instanceof JarURLConnection) {
                        checkJarFile((JarURLConnection) connection, pckgname, result);
                    } else if (connection instanceof FileURLConnection) {
                        try {
                            checkDirectory(new File(URLDecoder.decode(url.getPath(), "UTF-8")),
                                    pckgname, result);
                        } catch (final UnsupportedEncodingException ex) {
                            throw new ClassNotFoundException(
                                    pckgname
                                    + " does not appear to be a valid package (Unsupported encoding)",
                                    ex);
                        }
                    } else {
                        throw new ClassNotFoundException(pckgname + " (" + url.getPath()
                                + ") does not appear to be a valid package");
                    }
                } catch (final IOException ioex) {
                    throw new ClassNotFoundException(
                            "IOException was thrown when trying to get all resources for "
                                    + pckgname, ioex);
                }
            }
        } catch (final NullPointerException ex) {
            throw new ClassNotFoundException(pckgname
                    + " does not appear to be a valid package (Null pointer exception)", ex);
        } catch (final IOException ioex) {
            throw new ClassNotFoundException(
                    "IOException was thrown when trying to get all resources for " + pckgname, ioex);
        }

        return result;
    }

    /**
     * Private helper method
     *
     * @param directory
     *            The directory to start with
     * @param pckgname
     *            The package name to search for. Will be needed for getting the Class object.
     * @param result
     *            if a file isn't loaded but still is in the directory
     * @throws ClassNotFoundException
     */
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

                        if (accept.clazz(clazz)) {
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

    /**
     * Private helper method.
     *
     * @param connection
     *            the connection to the jar
     * @param pckgname
     *            the package name to search for
     * @param result
     *            the current ArrayList of all classes. This method will simply add new classes.
     * @throws ClassNotFoundException
     *             if a file isn't loaded but still is in the jar file
     * @throws IOException
     *             if it can't correctly read from the jar file.
     */
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
                    if (accept.clazz(clazz)) {
                        result.add(clazz);
                    }
                }
            }
        }

    }

}
