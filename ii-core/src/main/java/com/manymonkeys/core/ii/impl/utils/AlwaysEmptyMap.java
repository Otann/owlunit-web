package com.manymonkeys.core.ii.impl.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
* @author Anton Chebotaev
*         Owls Proprietary
*/
public final class AlwaysEmptyMap implements Map {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(Object o) {
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        return false;
    }

    @Override
    public Object get(Object o) {
        return null;
    }

    @Override
    public Object put(Object o, Object o1) {
        return null;
    }

    @Override
    public Object remove(Object o) {
        return null;
    }

    @Override
    public void putAll(Map map) { }

    @Override
    public void clear() { }

    @Override
    public Set keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection values() {
        return Collections.emptySet();
    }

    @Override
    public Set entrySet() {
        return Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
