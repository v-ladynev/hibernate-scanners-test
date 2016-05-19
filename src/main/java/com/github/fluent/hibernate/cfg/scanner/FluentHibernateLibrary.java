package com.github.fluent.hibernate.cfg.scanner;

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
            return EntityScanner
                    .scanPackages(packagesToScan, Arrays.asList(getLoader()), getAnnotation())
                    .result();
        }

        return EntityScanner.scanPackages(packagesToScan).result();
    }

}
