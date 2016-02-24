package com.github.ladynev.scanners;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import com.github.ladynev.scanners.util.ClassUtils;

import eu.infomas.annotation.AnnotationDetector;
import eu.infomas.annotation.AnnotationDetector.TypeReporter;

/**
 * https://github.com/rmuller/infomas-asl
 *
 * @author V.Ladynev
 */
public class InfomasAslLibrary implements IScanner {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        EntityReporter reporter = new EntityReporter();
        new AnnotationDetector(reporter).detect(packageToScan);
        return reporter.getResult();
    }

    private static class EntityReporter implements TypeReporter {

        final List<Class<?>> result = new ArrayList<Class<?>>();

        public List<Class<?>> getResult() {
            return result;
        }

        @Override
        public Class<? extends Annotation>[] annotations() {
            return new Class[] { Entity.class };
        }

        @Override
        public void reportTypeAnnotation(Class<? extends Annotation> annotation, String className) {
            result.add(ClassUtils.toClass(className));
        }

    }

}
