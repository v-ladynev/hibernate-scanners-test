package com.github.ladynev.scanners.fluent;

import java.util.Arrays;
import java.util.List;

import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class FluentHibernateLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String... packagesToScan) throws Exception {
        if (isTuned()) {
            return FluentEntityScanner.scanPackages(packagesToScan, Arrays.asList(getLoader()),
                    getAnnotation());
        }

        return FluentEntityScanner.scanPackages(packagesToScan);
    }

}
