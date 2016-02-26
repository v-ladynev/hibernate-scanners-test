package com.github.ladynev.scanners;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.github.ladynev.scanners.util.ClassUtils;
import com.github.ladynev.scanners.util.ScannerAdapter;

import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.TypeReporter;

/**
 * https://github.com/rmuller/infomas-asl
 *
 * @author V.Ladynev
 */
public class InfomasAslLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        replaceContextClassLoader();

        EntityReporter reporter = new EntityReporter();
        new AnnotationDetector(reporter).detect(packageToScan);
        List<Class<?>> result = reporter.getResult();

        backContextClassLoader();

        return result;
    }

    private class EntityReporter implements TypeReporter {

        final List<Class<?>> result = new ArrayList<Class<?>>();

        public List<Class<?>> getResult() {
            return result;
        }

        @Override
        public Class<? extends Annotation>[] annotations() {
            return new Class[] { InfomasAslLibrary.this.getAnnotation() };
        }

        @Override
        public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
            result.add(ClassUtils.toClass(className, InfomasAslLibrary.this.getLoader()));
        }

    }

}
