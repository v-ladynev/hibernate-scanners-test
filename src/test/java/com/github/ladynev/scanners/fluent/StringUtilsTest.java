/**
 * $HeadURL$
 * $Revision$
 * $Date$
 *
 * Copyright (c) Isida-Informatica, Ltd. All Rights Reserved.
 */
package com.github.ladynev.scanners.fluent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 *
 * @author V.Ladynev
 * @version $Id$
 */
public class StringUtilsTest {

    @Test
    public void splitBySpace() {
        assertThat(StringUtils.splitBySpace(null)).hasSize(0);
        assertThat(StringUtils.splitBySpace("")).hasSize(0);
        assertThat(StringUtils.splitBySpace("  ")).hasSize(0);
        assertThat(StringUtils.splitBySpace("a")).containsExactly("a");
        assertThat(StringUtils.splitBySpace("a  b")).containsExactly("a", "b");
        assertThat(StringUtils.splitBySpace(" a  b ")).containsExactly("a", "b");
    }

}
