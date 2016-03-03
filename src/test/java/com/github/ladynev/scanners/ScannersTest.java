package com.github.ladynev.scanners;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

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
import com.github.ladynev.scanners.util.IScanner;
import com.github.ladynev.scanners.util.ScannersUtils;
import com.google.common.collect.ObjectArrays;

/**
 *
 * @author V.Ladynev
 */
public class ScannersTest {

    private static final String ROOT_PACKAGE = "com.github.ladynev.scanners.persistent";

    private static final String OTHER_PACKAGE = "com.github.ladynev.scanners.other.persistent";

    private static final Class<?>[] SIMPLY_ENTITY_CLASSES = new Class<?>[] { FirstRootEntity.class,
            FirstRootEntity.NestedEntity.class, SecondRootEntity.class,
            FirstSubpackageEntity.class, NestedEntity.class };

    private static final Class<?>[] JAR_STATIC_ENTITY_CLASSES = new Class<?>[] {
            FirstRootEntityJar.class, FirstRootEntityJar.NestedEntityJar.class,
            SecondRootEntityJar.class, FirstSubpackageEntityJar.class, NestedEntityJar.class };

    private static final Class<?>[] ENTITY_CLASSES = ObjectArrays.concat(SIMPLY_ENTITY_CLASSES,
            JAR_STATIC_ENTITY_CLASSES, Class.class);

    private static final Class<?>[] OTHER_ENTITY_CLASSES = new Class<?>[] { OtherRootEntity.class,
            OtherRootEntity.OtherNestedEntity.class };

    private static final String JAR_DYNAMIC_ROOT_PACKAGE = "com.github.ladynev.scanners.jar.dyn.persistent";

    private static final Class<?>[] JAR_DYNAMIC_ENTITY_CLASSES = new Class<?>[] {
            FirstRootJarDynEntity.class, FirstRootJarDynEntity.NestedJarDynEntity.class,
            SecondRootJarDynEntity.class, FirstSubpackageJarDynEntity.class };

    private static final Class<?>[] JAR_DYNAMIC_CLASSES = ObjectArrays.concat(
            JAR_DYNAMIC_ENTITY_CLASSES, NotJarDynEntity.class);

    // @Test
    public void guavaLibrary() throws Exception {
        scan(new GuavaLibrary());
    }

    // @Test
    public void guavaLibraryJar() throws Exception {
        scanInDynamicJar(new GuavaLibrary());
    }

    // @Test
    public void springLibrary() throws Exception {
        scan(new SpringLibrary());
    }

    // @Test
    public void springLibraryJar() throws Exception {
        scanInDynamicJar(new SpringLibrary());
    }

    // @Test
    public void springOrmLibrary() throws Exception {
        scan(new SpringOrmLibrary());
    }

    // @Test
    public void springOrmLibraryJar() throws Exception {
        scanInDynamicJar(new SpringOrmLibrary());
    }

    // @Test
    public void springOrmCopyLibrary() throws Exception {
        scan(new SpringOrmCopyLibrary());
    }

    // @Test
    public void springOrmCopyLibraryJar() throws Exception {
        scanInDynamicJar(new SpringOrmCopyLibrary());
    }

    // @Test
    public void javaTool() throws Exception {
        scan(new JavaToolsScanner());
    }

    // @Test
    public void javaToolJar() throws Exception {
        scanInDynamicJar(new JavaToolsScanner());
    }

    // @Test
    public void customScanner() throws Exception {
        scan(new CustomScanner());
    }

    // @Test
    public void customScannerJar() throws Exception {
        scanInDynamicJar(new CustomScanner());
    }

    // @Test
    public void fastClasspathScannerLibrary() throws Exception {
        scan(new FastClasspathScannerLibrary());
    }

    // @Test
    public void fastClasspathScannerLibraryJar() throws Exception {
        // FastClasspathScanner.verbose = true;
        scanInDynamicJar(new FastClasspathScannerLibrary());
    }

    // @Test
    public void infomasAslLibrary() throws Exception {
        scan(new InfomasAslLibrary());
    }

    // @Test
    public void infomasAslLibraryJar() throws Exception {
        scanInDynamicJar(new InfomasAslLibrary());
    }

    // @Test
    public void classEnumerator() throws Exception {
        scan(new ClassEnumeratorScanner());
    }

    // @Test
    public void classEnumeratorJar() throws Exception {
        scanInDynamicJar(new ClassEnumeratorScanner());
    }

    // @Test
    public void reflectionsLibrary() throws Exception {
        // ClasspathHelper working with loaders
        scan(new ReflectionsLibrary());
    }

    // @Test
    public void reflectionsLibraryJar() throws Exception {
        scanInDynamicJar(new ReflectionsLibrary());
    }

    @Test
    public void reflectionsCopyLibrary() throws Exception {
        scan(new ReflectionsCopyLibrary());
    }

    // @Test
    public void reflectionsCopyLibraryJar() throws Exception {
        scanInDynamicJar(new ReflectionsCopyLibrary());
    }

    // @Test
    public void annoventionsLibrary() throws Exception {
        scan(new AnnoventionLibrary());
    }

    // @Test
    public void annoventionsLibraryJar() throws Exception {
        scanInDynamicJar(new AnnoventionLibrary());
    }

    // @Test
    public void fluentHibernateLibrary() throws Exception {
        // guava 1010 millis
        scan(new FluentHibernateLibrary());
    }

    // @Test
    public void fluentHibernateLibraryJar() throws Exception {
        scanInDynamicJar(new FluentHibernateLibrary());
    }

    private static void scan(IScanner scanner) throws Exception {
        long begin = System.currentTimeMillis();
        List<Class<?>> classes = scanner.scan(ROOT_PACKAGE, OTHER_PACKAGE);
        long elapsed = System.currentTimeMillis() - begin;
        System.out.println(String.format("%s: %d millis", scanner.getClass().getSimpleName(),
                elapsed));

        assertThat(classes).contains(ENTITY_CLASSES).contains(OTHER_ENTITY_CLASSES)
        .doesNotContain(NotEntity.class, NotEntityJar.class);
    }

    private static void scanInDynamicJar(IScanner scanner) throws Exception {
        File jarFile = File.createTempFile("scanners-test", ".jar");
        try {
            scanInDynamicJar(scanner, jarFile);
        } finally {
            jarFile.delete();
        }
    }

    private static void scanInDynamicJar(IScanner scanner, File jarFile) throws Exception {
        ScannersTestUtils.writeJarFile(jarFile, JAR_DYNAMIC_CLASSES);

        URLClassLoader loader = createDynJarClassLoader(jarFile);

        Class<? extends Annotation> entityAnnotation = (Class<? extends Annotation>) loader
                .loadClass(Entity.class.getName());
        assertThat(entityAnnotation).isNotNull();

        scanner.tune(loader, entityAnnotation);
        List<Class<?>> classes = scanner.scan(JAR_DYNAMIC_ROOT_PACKAGE);

        assertThat(classes).contains(reload(loader, JAR_DYNAMIC_ENTITY_CLASSES)).doesNotContain(
                reload(loader, NotJarDynEntity.class));

        assertThat(classes).doesNotContain(JAR_DYNAMIC_ENTITY_CLASSES);
    }

    private static URLClassLoader createDynJarClassLoader(File jarFile) throws Exception {
        URL jpaJar = ScannersUtils.urlForJar("hibernate-jpa-2.1-api-1.0.0.Final.jar");
        assertThat(jpaJar).isNotNull();

        ClassLoader parent = null;
        return ScannersUtils.createClassLoader(parent, jarFile.toURI().toURL(), jpaJar);
    }

    private static Class<?>[] reload(ClassLoader loader, Class<?>... classes) throws Exception {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>(classes.length);
        for (Class<?> clazz : classes) {
            result.add(loader.loadClass(clazz.getName()));
        }

        return result.toArray(new Class<?>[result.size()]);
    }

}
