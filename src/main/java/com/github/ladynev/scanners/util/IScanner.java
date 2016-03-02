package com.github.ladynev.scanners.util;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 * @author V.Ladynev
 */
public interface IScanner {

    List<Class<?>> scan(String packageToScan) throws Exception;

    List<Class<?>> scan(String... packagesToScan) throws Exception;

    void tune(ClassLoader loader, Class<? extends Annotation> annotation);

}
