package com.github.ladynev.scanners.fluent;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import javax.persistence.Entity;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.ladynev.scanners.persistent.FirstRootEntity;
import com.github.ladynev.scanners.persistent.NotEntity;

/**
 *
 * @author V.Ladynev
 */
public class AnnotationsCheckerTest {

    private static AnnotationChecker annotationChecker;

    @BeforeClass
    public static void init() {
        annotationChecker = new AnnotationChecker(Entity.class);
    }

    @Test
    public void checkInPackage() throws Exception {
        assertThat(check(FirstRootEntity.class)).isTrue();
        assertThat(check(FirstRootEntity.NestedEntity.class)).isTrue();
        assertThat(check(NotEntity.class)).isFalse();
    }

    private static boolean check(Class<?> clazz) throws Exception {
        return annotationChecker.detect(classAsStream(clazz));
    }

    private static InputStream classAsStream(Class<?> clazz) {
        return clazz.getResourceAsStream("/" + ClassUtils.packageAsResourcePath(clazz.getName())
                + ".class");
    }

}
