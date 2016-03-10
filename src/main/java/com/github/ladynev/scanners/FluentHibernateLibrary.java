package com.github.ladynev.scanners;

import java.util.List;

import com.github.ladynev.scanners.fluent.FluentEntityScanner;
import com.github.ladynev.scanners.util.ScannerAdapter;

/**
 *
 * @author V.Ladynev
 */
public class FluentHibernateLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String... packagesToScan) throws Exception {
        return FluentEntityScanner.createForPackages(packagesToScan)
                .usingLoaders(isTuned() ? getLoader() : null).scan(getAnnotation());
    }

}
