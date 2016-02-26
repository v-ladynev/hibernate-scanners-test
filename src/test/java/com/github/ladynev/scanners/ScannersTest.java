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

/**
 *
 * @author V.Ladynev
 * @version $Id$
 */
public class ScannersTest {

    private static final String ROOT_PACKAGE = "com.github.ladynev.scanners.persistent";

    @Test
    public void guavaLibrary() throws Exception {
        scan(new GuavaLibrary());
    }

    @Test
    public void guavaLibraryJar() throws Exception {
        scanInJar(new GuavaLibrary());
    }

    // @Test
    public void springLibrary() throws Exception {
        scan(new SpringLibrary());
    }

    // @Test
    public void springLibraryJar() throws Exception {
        scanInJar(new SpringLibrary());
    }

    // @Test
    public void javaTool() throws Exception {
        scan(new JavaToolsScanner());
    }

    // @Test
    public void javaToolJar() throws Exception {
        scanInJar(new JavaToolsScanner());
    }

    // @Test
    public void customScanner() throws Exception {
        scan(new CustomScanner());
    }

    // @Test
    public void customScannerJar() throws Exception {
        scanInJar(new CustomScanner());
    }

    // @Test
    public void fastClasspathScannerLibrary() throws Exception {
        scan(new FastClasspathScannerLibrary());
    }

    // @Test
    public void fastClasspathScannerLibraryJar() throws Exception {
        scanInJar(new FastClasspathScannerLibrary());
    }

    // @Test
    public void infomasAslLibrary() throws Exception {
        scan(new InfomasAslLibrary());
    }

    // @Test
    public void infomasAslLibraryJar() throws Exception {
        scanInJar(new InfomasAslLibrary());
    }

    // @Test
    public void classEnumerator() throws Exception {
        scan(new ClassEnumeratorScanner());
    }

    // @Test
    public void classEnumeratorJar() throws Exception {
        scanInJar(new ClassEnumeratorScanner());
    }

    // @Test
    public void reflectionsLibrary() throws Exception {
        scan(new ReflectionsLibrary());
    }

    // @Test
    public void reflectionsLibraryJar() throws Exception {
        scanInJar(new ReflectionsLibrary());
    }

    // @Test
    public void annoventionsLibrary() throws Exception {
        scan(new AnnoventionLibrary());
    }

    // @Test
    public void annoventionsLibraryJar() throws Exception {
        scanInJar(new AnnoventionLibrary());
    }

    private static void scan(IScanner scanner) throws Exception {
        List<Class<?>> classes = scan(scanner, ROOT_PACKAGE);
        assertThat(classes).contains(FirstRootEntity.class, SecondRootEntity.class,
                FirstSubpackageEntity.class, NestedEntity.class, FirstRootEntityJar.class,
                SecondRootEntityJar.class, FirstSubpackageEntityJar.class, NestedEntityJar.class)
                .doesNotContain(NotEntity.class, NotEntityJar.class);
    }

    private static List<Class<?>> scan(IScanner scanner, String... packages) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (String packageToScan : packages) {
            result.addAll(scanner.scan(packageToScan));
        }

        return result;
    }

    private static void scanInJar(IScanner scanner) throws Exception {
        File jarFile = File.createTempFile("scanners-test", ".jar");
        try {
            scanInJar(scanner, jarFile);
        } finally {
            jarFile.delete();
        }
    }

    private static void scanInJar(IScanner scanner, File jarFile) throws Exception {
        ScannersTestUtils.writeJarFile(jarFile, FirstRootEntity.class,
                FirstRootEntity.NestedEntity.class, SecondRootEntity.class,
                FirstSubpackageEntity.class, NotEntity.class);

        URL jpaJar = ClassUtils.urlForJar("hibernate-jpa-2.1-api-1.0.0.Final.jar");
        assertThat(jpaJar).isNotNull();

        ClassLoader parent = null;
        URLClassLoader loader = ClassUtils.createClassLoader(parent, jarFile.toURI().toURL(),
                jpaJar);

        Class<? extends Annotation> entityAnnotation = (Class<? extends Annotation>) loader
                .loadClass(Entity.class.getName());
        assertThat(entityAnnotation).isNotNull();

        scanner.tune(loader, entityAnnotation);
        List<Class<?>> classes = scanner.scan(ROOT_PACKAGE);

        assertThat(classes).contains(
                reload(loader, FirstRootEntity.class, SecondRootEntity.class,
                        FirstSubpackageEntity.class, NestedEntity.class)).doesNotContain(
                                reload(loader, NotEntity.class));

        assertThat(classes).doesNotContain(FirstRootEntity.class, SecondRootEntity.class,
                FirstSubpackageEntity.class, NestedEntity.class);
    }

    private static Class<?>[] reload(ClassLoader loader, Class<?>... classes) throws Exception {
        ArrayList<Class<?>> result = new ArrayList<Class<?>>(classes.length);
        for (Class<?> clazz : classes) {
            result.add(loader.loadClass(clazz.getName()));
        }

        return result.toArray(new Class<?>[result.size()]);
    }

}
