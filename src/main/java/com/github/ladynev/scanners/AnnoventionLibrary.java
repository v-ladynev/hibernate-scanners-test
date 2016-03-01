package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import com.github.ladynev.scanners.util.ScannersUtils;
import com.github.ladynev.scanners.util.ScannerAdapter;
import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationDiscoveryListener;

/**
 * https://github.com/ngocdaothanh/annovention
 *
 * @author V.Ladynev
 */
public class AnnoventionLibrary extends ScannerAdapter {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        replaceContextClassLoader();

        EntityListener listener = new EntityListener();

        Discoverer discoverer = new ClasspathDiscoverer();
        discoverer.addAnnotationListener(listener);

        final boolean classes = true;
        final boolean visible = true;
        final boolean invisible = true;
        discoverer.discover(classes, false, false, visible, invisible);

        List<Class<?>> result = listener.getResult();

        backContextClassLoader();

        return result;
    }

    private class EntityListener implements ClassAnnotationDiscoveryListener {

        final List<Class<?>> result = new ArrayList<Class<?>>();

        public List<Class<?>> getResult() {
            return result;
        }

        @Override
        public void discovered(String clazz, String annotation) {
            result.add(ScannersUtils.toClass(clazz, AnnoventionLibrary.this.getLoader()));
        }

        @Override
        public String[] supportedAnnotations() {
            return new String[] { AnnoventionLibrary.this.getAnnotation().getName() };
        }

    }

}
