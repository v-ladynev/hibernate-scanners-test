package com.github.ladynev.scanners;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.junit.Test;

import com.github.ladynev.scanners.IScanner.IAccept;
import com.github.ladynev.scanners.persistent.FirstRootEntity;
import com.github.ladynev.scanners.persistent.FirstRootEntity.NestedEntity;
import com.github.ladynev.scanners.persistent.NotEntity;
import com.github.ladynev.scanners.persistent.SecondRootEntity;
import com.github.ladynev.scanners.persistent.subpackage.FirstSubpackageEntity;

/**
 *
 * @author V.Ladynev
 * @version $Id$
 */
public class ScannersTest {

    private static final String ROOT_PACKAGE = "com.github.ladynev.scanners.persistent";

    // @Test
    public void guavaLibrary() throws Exception {
        assertClasses(scan(new GuavaLibrary(), ROOT_PACKAGE));
    }

    // @Test
    public void javaTool() throws Exception {
        assertClasses(scan(new JavaToolsScanner(), ROOT_PACKAGE));
    }

    // @Test
    public void customScanner() throws Exception {
        assertClasses(scan(new CustomScanner(), ROOT_PACKAGE));
    }

    // @Test
    public void fastClasspathScannerLibrary() throws Exception {
        // TODO add other packages
        List<Class<?>> classes = new FastClasspathScannerLibrary().scan(ROOT_PACKAGE);
        assertClasses(classes);
    }

    // @Test
    public void infomasAslLibrary() throws Exception {
        assertClasses(scan(new InfomasAslLibrary(), ROOT_PACKAGE));
    }

    // @Test
    public void classEnumerator() throws Exception {
        assertClasses(scan(new ClassEnumeratorScanner(), ROOT_PACKAGE));
    }

    // @Test
    public void reflectionsLibrary() throws Exception {
        assertClasses(scan(new ReflectionsLibrary(), ROOT_PACKAGE));
    }

    @Test
    public void annoventionsLibrary() throws Exception {
        assertClasses(scan(new AnnoventionLibrary(), ROOT_PACKAGE));
    }

    private void assertClasses(List<Class<?>> classes) {
        assertThat(classes).contains(FirstRootEntity.class, SecondRootEntity.class,
                FirstSubpackageEntity.class, NestedEntity.class).doesNotContain(NotEntity.class);
    }

    public static List<Class<?>> scan(IScanner scanner, String... packages) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (String packageToScan : packages) {
            result.addAll(scanner.scan(packageToScan, new IAccept() {
                @Override
                public boolean clazz(Class<?> toCheck) throws Exception {
                    return toCheck.isAnnotationPresent(Entity.class);
                }
            }));
        }

        return result;
    }
}
