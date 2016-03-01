package com.github.ladynev.scanners;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.ClassAnnotationMatchProcessor;

import java.util.ArrayList;
import java.util.List;

import com.github.ladynev.scanners.util.ScannerAdapter;
import com.github.ladynev.scanners.util.ScannersUtils;

/**
 * https://github.com/lukehutch/fast-classpath-scanner
 *
 * @author V.Ladynev
 */
public class FastClasspathScannerLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        final List<Class<?>> result = new ArrayList<Class<?>>();

        replaceContextClassLoader();

        new FastClasspathScanner(new String[] { packageToScan }).matchClassesWithAnnotation(
                getAnnotation(), new ClassAnnotationMatchProcessor() {
                    @Override
                    public void processMatch(Class<?> matchingClass) {
                        result.add(ScannersUtils.toClass(matchingClass.getName(), getLoader()));
                    }
                }).scan();

        backContextClassLoader();

        return result;
    }
}
