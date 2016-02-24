package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import org.reflections.Reflections;

/**
 *
 * @author V.Ladynev
 */
public class ReflectionsScanner implements IScanner {

    @Override
    public List<Class<?>> scan(String packageToScan, IAccept accept) throws Exception {
        Reflections reflections = new Reflections(packageToScan);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
        return new ArrayList<Class<?>>(annotated);
    }

}
