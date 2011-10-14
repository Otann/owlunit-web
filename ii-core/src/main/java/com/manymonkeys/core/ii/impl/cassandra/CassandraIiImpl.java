package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraIiImpl implements Ii, Comparable<Ii> {

    UUID uuid;
    Map<String, String> meta;
    Map<Ii, Double> components;
    Map<Ii, Double> parents;

    CassandraIiImpl(UUID uuid) {
        this.uuid = uuid;
    }

    CassandraIiImpl(CassandraIiImpl item) {
        this.uuid = item.uuid;
        this.meta = item.meta;
        this.parents = item.parents;
        this.components = item.components;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Map<String, String> getMetaMap() {
        return meta;
    }

    @Override
    public String getMeta(String key) {
        return meta.get(key);
    }

    @Override
    public Map<Ii, Double> getComponents() {
        return components;
    }

    @Override
    public Double getComponentWeight(Ii component) {
        return components.get(component);
    }

    @Override
    public Map<Ii, Double> getParents() {
        return parents;
    }

    @Override
    public Double getParentWeight(Ii parent) {
        return parents.get(parent);
    }


    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ii) {
            Ii local = (Ii) obj;
            return uuid.equals(local.getUUID());
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Ii item) {
        return uuid.compareTo(item.getUUID());
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
