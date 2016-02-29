package com.github.ladynev.scanners;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import com.github.ladynev.scanners.jar.tmp.persistent.FirstRootJarTmpEntity;
import com.github.ladynev.scanners.jar.tmp.persistent.NotJarTmpEntity;
import com.github.ladynev.scanners.jar.tmp.persistent.SecondRootJarTmpEntity;
import com.github.ladynev.scanners.jar.tmp.persistent.subpackage.FirstSubpackageJarTmpEntity;
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
import com.github.ladynev.scanners.util.ClassUtils;
import com.github.ladynev.scanners.util.IScanner;
import com.google.common.collect.ObjectArrays;

/**
 *
 * @author V.Ladynev
 */
public class ScannersTest {

    private static final String ROOT_PACKAGE = "com.github.ladynev.scanners.persistent";

    private static final Class<?>[] SIMPLY_ENTITY_CLASSES = new Class<?>[] { FirstRootEntity.class,
        SecondRootEntity.class, FirstSubpackageEntity.class, NestedEntity.class };

    private static final Class<?>[] JAR_STATIC_ENTITY_CLASSES = new Class<?>[] {
        FirstRootEntityJar.class, SecondRootEntityJar.class, FirstSubpackageEntityJar.class,
        NestedEntityJar.class };

    private static final Class<?>[] ENTITY_CLASSES = ObjectArrays.concat(SIMPLY_ENTITY_CLASSES,
            JAR_STATIC_ENTITY_CLASSES, Class.class);

    private static final String JAR_TMP_ROOT_PACKAGE = "com.github.ladynev.scanners.jar.tmp.persistent";

    private static final Class<?>[] JAR_TMP_ENTITY_CLASSES = new Class<?>[] {
        FirstRootJarTmpEntity.class, FirstRootJarTmpEntity.NestedJarTmpEntity.class,
        SecondRootJarTmpEntity.class, FirstSubpackageJarTmpEntity.class };

    private static final Class<?>[] JAR_TMP_CLASSES = ObjectArrays.concat(JAR_TMP_ENTITY_CLASSES,
            NotJarTmpEntity.class);

    // @Test
    public void guavaLibrary() throws Exception {
        scan(new GuavaLibrary());
    }

    // @Test
    public void guavaLibraryJar() throws Exception {
        scanInJarTmp(new GuavaLibrary());
    }

    // @Test
    public void springLibrary() throws Exception {
        scan(new SpringLibrary());
    }

    // @Test
    public void springLibraryJar() throws Exception {
        scanInJarTmp(new SpringLibrary());
    }

    // @Test
    public void springOrmLibrary() throws Exception {
        scan(new SpringOrmLibrary());
    }

    // @Test
    public void springOrmLibraryJar() throws Exception {
        scanInJarTmp(new SpringOrmLibrary());
    }

    // @Test
    public void javaTool() throws Exception {
        scan(new JavaToolsScanner());
    }

    // @Test
    public void javaToolJar() throws Exception {
        scanInJarTmp(new JavaToolsScanner());
    }

    // @Test
    public void customScanner() throws Exception {
        scan(new CustomScanner());
    }

    // @Test
    public void customScannerJar() throws Exception {
        scanInJarTmp(new CustomScanner());
    }

    // @Test
    public void fastClasspathScannerLibrary() throws Exception {
        scan(new FastClasspathScannerLibrary());
    }

    // @Test
    public void fastClasspathScannerLibraryJar() throws Exception {
        scanInJarTmp(new FastClasspathScannerLibrary());
    }

    // @Test
    public void infomasAslLibrary() throws Exception {
        scan(new InfomasAslLibrary());
    }

    // @Test
    public void infomasAslLibraryJar() throws Exception {
        scanInJarTmp(new InfomasAslLibrary());
    }

    // @Test
    public void classEnumerator() throws Exception {
        scan(new ClassEnumeratorScanner());
    }

    // @Test
    public void classEnumeratorJar() throws Exception {
        scanInJarTmp(new ClassEnumeratorScanner());
    }

    // @Test
    public void reflectionsLibrary() throws Exception {
        scan(new ReflectionsLibrary());
    }

    // @Test
    public void reflectionsLibraryJar() throws Exception {
        scanInJarTmp(new ReflectionsLibrary());
    }

    // @Test
    public void annoventionsLibrary() throws Exception {
        scan(new AnnoventionLibrary());
    }

    // @Test
    public void annoventionsLibraryJar() throws Exception {
        scanInJarTmp(new AnnoventionLibrary());
    }

    private static void scan(IScanner scanner) throws Exception {
        List<Class<?>> classes = scanner.scan(ROOT_PACKAGE);
        assertThat(classes).contains(ENTITY_CLASSES).doesNotContain(NotEntity.class,
                NotEntityJar.class);
    }

    private static void scanInJarTmp(IScanner scanner) throws Exception {
        File jarFile = File.createTempFile("scanners-test", ".jar");
        try {
            scanInJarTmp(scanner, jarFile);
        } finally {
            jarFile.delete();
        }
    }

    private static void scanInJarTmp(IScanner scanner, File jarFile) throws Exception {
        ScannersTestUtils.writeJarFile(jarFile, JAR_TMP_CLASSES);

        URLClassLoader loader = createTmpJarClassLoader(jarFile);

        Class<? extends Annotation> entityAnnotation = (Class<? extends Annotation>) loader
                .loadClass(Entity.class.getName());
        assertThat(entityAnnotation).isNotNull();

        scanner.tune(loader, entityAnnotation);
        List<Class<?>> classes = scanner.scan(JAR_TMP_ROOT_PACKAGE);

        assertThat(classes).contains(reload(loader, JAR_TMP_ENTITY_CLASSES)).doesNotContain(
                reload(loader, NotJarTmpEntity.class));

        assertThat(classes).doesNotContain(JAR_TMP_ENTITY_CLASSES);
    }

    private static URLClassLoader createTmpJarClassLoader(File jarFile) throws Exception {
        URL jpaJar = ClassUtils.urlForJar("hibernate-jpa-2.1-api-1.0.0.Final.jar");
        assertThat(jpaJar).isNotNull();

        ClassLoader parent = null;
        return ClassUtils.createClassLoader(parent, jarFile.toURI().toURL(), jpaJar);
    }

    private static Class<?>[] reload(ClassLoader loader, Class<?>... classes) throws Exception {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>(classes.length);
        for (Class<?> clazz : classes) {
            result.add(loader.loadClass(clazz.getName()));
        }

        return result.toArray(new Class<?>[result.size()]);
    }

}
