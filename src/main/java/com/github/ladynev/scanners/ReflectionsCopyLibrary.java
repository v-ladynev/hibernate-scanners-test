package com.github.ladynev.scanners;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.reflections.Reflections;

import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 * https://github.com/ronmamo/reflections
 *
 * @author V.Ladynev
 */
public class ReflectionsCopyLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String... packagesToScan) throws Exception {
        ArrayList<URL> list = Collections.list(getLoader().getResources("META-INF"));
        System.out.println(list);
        // System.out.println(Arrays.asList(((URLClassLoader) getLoader()).getURLs()));

        URL url = list.get(0);
        JarURLConnection urlcon = (JarURLConnection) url.openConnection();
        JarFile jar = urlcon.getJarFile();
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            String entry = entries.nextElement().getName();
            System.out.println(entry);
        }

        Reflections reflections = isTuned() ? new Reflections(packagesToScan, getLoader())
                : new Reflections(packagesToScan);

        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(getAnnotation());
        return new ArrayList<Class<?>>(annotated);
    }

}
