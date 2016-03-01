package com.github.ladynev.scanners.jar.dyn.persistent;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author V.Ladynev
 */
@Entity
@Table
public class FirstRootJarDynEntity {

    @Entity
    @Table
    public static class NestedJarDynEntity {

    }

}
