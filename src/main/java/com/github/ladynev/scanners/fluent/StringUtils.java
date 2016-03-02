package com.github.ladynev.scanners.fluent;

/**
 *
 * @author V.Ladynev
 */
final class StringUtils {

    public static final String EMPTY = "";

    private StringUtils() {

    }

    public static String[] splitBySpace(String value) {
        if (value == null) {
            return new String[0];
        }

        value = value.trim();

        if (value.length() == 0) {
            return new String[0];
        }

        return value.split("\\s+");
    }

}
