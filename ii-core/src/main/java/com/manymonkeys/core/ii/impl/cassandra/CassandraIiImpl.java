package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;

import java.util.Map;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraIiImpl implements Ii, Comparable<Ii> {

    UUID id;
    Map<String, String> meta;
    Map<Ii, Double> components;
    Map<Ii, Double> parents;

    CassandraIiImpl(UUID uuid) {
        this.id = uuid;
    }

    CassandraIiImpl(CassandraIiImpl item) {
        this.id = item.id;
        this.meta = item.meta;
        this.parents = item.parents;
        this.components = item.components;
    }

    @Override
    public UUID getUUID() {
        return id;
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
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ii) {
            Ii local = (Ii) obj;
            return id.equals(local.getUUID());
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Ii item) {
        return id.compareTo(item.getUUID());
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
