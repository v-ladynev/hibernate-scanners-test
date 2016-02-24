package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.ClassAnnotationDiscoveryListener;

/**
 * https://github.com/ngocdaothanh/annovention
 *
 * @author V.Ladynev
 */
public class AnnoventionLibrary implements IScanner {

    @Override
    public List<Class<?>> scan(String packageToScan, IAccept accept) throws Exception {
        EntityListener listener = new EntityListener();

        Discoverer discoverer = new ClasspathDiscoverer();
        discoverer.addAnnotationListener(listener);

        boolean classes = true;
        boolean visible = true;
        boolean invisible = true;
        discoverer.discover(classes, false, false, visible, invisible);

        return listener.getResult();
    }

    private static class EntityListener implements ClassAnnotationDiscoveryListener {

        final List<Class<?>> result = new ArrayList<Class<?>>();

        public List<Class<?>> getResult() {
            return result;
        }

        @Override
        public void discovered(String clazz, String annotation) {
            result.add(ClassUtils.toClass(clazz));
        }

        @Override
        public String[] supportedAnnotations() {
            return new String[] { Entity.class.getName() };
        }

    }

}
