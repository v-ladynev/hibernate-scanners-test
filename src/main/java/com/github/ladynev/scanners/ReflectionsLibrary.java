package com.github.ladynev.scanners;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;

import org.reflections.Reflections;

/**
 * https://github.com/ronmamo/reflections
 *
 * @author V.Ladynev
 */
public class ReflectionsLibrary implements IScanner {

    @Override
    public List<Class<?>> scan(String packageToScan) throws Exception {
        Reflections reflections = new Reflections(packageToScan);
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Entity.class);
        return new ArrayList<Class<?>>(annotated);
    }

}
