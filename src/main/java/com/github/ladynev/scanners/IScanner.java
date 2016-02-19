package com.github.ladynev.scanners;

import java.util.List;

/**
 *
 * @author V.Ladynev
 */
public interface IScanner {

    List<Class<?>> scan(String packageToScan, List<Class<?>> result) throws Exception;

}
