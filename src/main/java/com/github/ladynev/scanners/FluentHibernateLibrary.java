package com.github.ladynev.scanners;

import java.util.List;

import com.github.ladynev.scanners.fluent.FluentHibernateScanner;
import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class FluentHibernateLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        replaceContextClassLoader();
        List<Class<?>> result = FluentHibernateScanner.scanPackages(getAnnotation(), packageToScan);
        backContextClassLoader();
        return result;
    }

}
