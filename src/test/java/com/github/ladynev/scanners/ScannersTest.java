package com.github.ladynev.scanners;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
    public void guava() throws Exception {
        assertClasses(scan(new GuavaScanner(), ROOT_PACKAGE));
    }

    @Test
    public void javaTool() throws Exception {
        assertClasses(scan(new JavaToolsScanner(), ROOT_PACKAGE));
    }

    private void assertClasses(List<Class<?>> classes) {
        assertThat(classes).contains(FirstRootEntity.class, SecondRootEntity.class,
                FirstSubpackageEntity.class, NestedEntity.class).doesNotContain(NotEntity.class);
    }

    public static List<Class<?>> scan(IScanner scanner, String... packages) throws Exception {
        List<Class<?>> result = new ArrayList<Class<?>>();
        for (String packageToScan : packages) {
            scanner.scan(packageToScan, result);
        }

        return result;
    }

}
