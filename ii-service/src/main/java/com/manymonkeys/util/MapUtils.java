package com.manymonkeys.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 *         <p/>
 *         This class is intended to allow this syntax:
 *         <p/>
 *         import static com.manymonkeys.util.MapUtils.*;
 *         ...
 *         Map<String, Integer> map = asMap(e("screw", 1), e("collections", 2), e("framework", 3));
 */
public class MapUtils {

    public static <K, V> Map<K, V> asMap(Map.Entry<K, V>... entries) {
        Map<K, V> map = new HashMap<K, V>();
        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Map.Entry<K, V> e(final K k, final V v) {
        return new Map.Entry<K, V>() {
            public K getKey() {
                return k;
            }

            public V getValue() {
                return v;
            }

            public V setValue(V value) {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }

}
