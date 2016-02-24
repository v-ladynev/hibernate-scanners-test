package com.github.ladynev.scanners;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

/**
 * https://github.com/lukehutch/fast-classpath-scanner
 *
 * @author V.Ladynev
 */
public class FastClasspathScannerLibrary {

    public List<Class<?>> scan(String packageToScan) throws Exception {
        final List<Class<?>> result = new ArrayList<Class<?>>();

        new FastClasspathScanner(new String[] { packageToScan }).matchClassesWithAnnotation(
                Entity.class, new ClassAnnotationMatchProcessor() {
                    @Override
                    public void processMatch(Class<?> matchingClass) {
                        result.add(matchingClass);
                    }
                }).scan();

        return result;
    }

}
