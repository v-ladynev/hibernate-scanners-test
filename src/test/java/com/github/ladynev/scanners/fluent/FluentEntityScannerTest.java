package com.github.ladynev.scanners.fluent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.ladynev.scanners.jar.dyn.persistent.FirstRootJarDynEntity;
import com.github.ladynev.scanners.jar.dyn.persistent.NotJarDynEntity;
import com.github.ladynev.scanners.jar.dyn.persistent.SecondRootJarDynEntity;
import com.github.ladynev.scanners.jar.dyn.persistent.subpackage.FirstSubpackageJarDynEntity;
import com.github.ladynev.scanners.other.persistent.OtherRootEntity;
import com.github.ladynev.scanners.persistent.FirstRootEntity;
import com.github.ladynev.scanners.persistent.FirstRootEntity.NestedEntity;
import com.github.ladynev.scanners.persistent.FirstRootEntityJar;
import com.github.ladynev.scanners.persistent.FirstRootEntityJar.NestedEntityJar;
import com.github.ladynev.scanners.persistent.NotEntity;
import com.github.ladynev.scanners.persistent.NotEntityJar;
import com.github.ladynev.scanners.persistent.SecondRootEntity;
import com.github.ladynev.scanners.persistent.SecondRootEntityJar;
import com.github.ladynev.scanners.persistent.subpackage.FirstSubpackageEntity;
import com.github.ladynev.scanners.persistent.subpackage.FirstSubpackageEntityJar;

/**
 *
 * @author V.Ladynev
 */
public class FluentEntityScannerTest {

    private static final Class<?>[] SIMPLY_ENTITY_CLASSES = new Class<?>[] { FirstRootEntity.class,
            FirstRootEntity.NestedEntity.class, SecondRootEntity.class,
            FirstSubpackageEntity.class, NestedEntity.class };

    private static final Class<?>[] OTHER_ENTITY_CLASSES = new Class<?>[] { OtherRootEntity.class,
            OtherRootEntity.OtherNestedEntity.class };

    private static final Class<?>[] JAR_STATIC_ENTITY_CLASSES = new Class<?>[] {
            FirstRootEntityJar.class, FirstRootEntityJar.NestedEntityJar.class,
            SecondRootEntityJar.class, FirstSubpackageEntityJar.class, NestedEntityJar.class };

    private static final Class<?>[] JAR_DYNAMIC_ENTITY_CLASSES = new Class<?>[] {
            FirstRootJarDynEntity.class, FirstRootJarDynEntity.NestedJarDynEntity.class,
            SecondRootJarDynEntity.class, FirstSubpackageJarDynEntity.class };

    private static final List<Class<?>> ENTITY_CLASSES = new ArrayList<Class<?>>() {
        {
            addAll(Arrays.asList(SIMPLY_ENTITY_CLASSES));
            addAll(Arrays.asList(OTHER_ENTITY_CLASSES));
            addAll(Arrays.asList(JAR_STATIC_ENTITY_CLASSES));
            addAll(Arrays.asList(JAR_DYNAMIC_ENTITY_CLASSES));
        }
    };

    @Test
    public void speedTest() throws Exception {
        long begin = System.currentTimeMillis();

        List<Class<?>> classes = FluentEntityScanner.scanPackages(null);

        long elapsed = System.currentTimeMillis() - begin;
        System.out.println(String.format("%d millis", elapsed));

        assertThat(classes).containsAll(ENTITY_CLASSES).doesNotContain(NotEntity.class,
                NotEntityJar.class, NotJarDynEntity.class);
    }

}
