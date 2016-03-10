package com.github.ladynev.scanners.fluent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author V.Ladynev
 */
public final class CollectionUtils {

    private CollectionUtils() {

    }

    public static <T> T first(List<T> items) {
        return isEmpty(items) ? null : items.get(0);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static <T> T[] correctToNull(T[] array) {
        return size(array) == 1 && array[0] == null ? null : array;
    }

    public static int size(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static <T> int size(T[] array) {
        return array == null ? 0 : array.length;
    }

    public static <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }

    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    public static <K> HashSet<K> newHashSet() {
        return new HashSet<K>();
    }

}
