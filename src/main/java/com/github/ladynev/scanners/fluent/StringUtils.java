package com.github.ladynev.scanners.fluent;


/**
 *
 * @author V.Ladynev
 */
final class StringUtils {

    public static final String EMPTY = "";

    private StringUtils() {

    }

    public static String correctEmpty(final String value) {
        return correctEmpty(value, EMPTY);
    }

    public static String correctEmpty(final String value, final String valueForEmpty) {
        return isEmpty(value) ? valueForEmpty : value;
    }

    public static boolean isEmpty(final String value) {
        if (value == null) {
            return true;
        }

        int len = value.length();
        if (len == 0) {
            return true;
        }

        if (value.charAt(0) > ' ') {
            return false;
        }

        for (int i = 1; i < len; i++) {
            if (value.charAt(i) > ' ') {
                return false;
            }
        }

        return true;
    }

    public static String[] splitBySpace(String value) {
        if (value == null) {
            return null;
        }

        return value.trim().split("\\s+");
    }

}
