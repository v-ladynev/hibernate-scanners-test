package com.github.ladynev.scanners.persistent;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author V.Ladynev
 */
@Entity
@Table
public class FirstRootEntity {

    @Entity
    @Table
    public static class NestedEntity {

    }

}
