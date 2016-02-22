package com.github.ladynev.scanners;

import java.util.List;

/**
 *
 * @author V.Ladynev
 */
public interface IScanner {

    List<Class<?>> scan(String packageToScan, IAccept accept) throws Exception;

    public interface IAccept {

        boolean clazz(Class<?> toCheck) throws Exception;

    }

}
