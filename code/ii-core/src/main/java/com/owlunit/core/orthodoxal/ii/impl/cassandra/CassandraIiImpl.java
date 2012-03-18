package com.owlunit.core.orthodoxal.ii.impl.cassandra;

import com.owlunit.core.orthodoxal.ii.Ii;

import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraIiImpl implements Ii, Comparable<Ii> {

    long id;
    Map<String, String> meta;
    Map<Ii, Double> components;
    Map<Ii, Double> parents;

    @SuppressWarnings("unchecked")
    CassandraIiImpl(long uuid) {
        this.id = uuid;
        this.meta = NOT_LOADED;
        this.parents = NOT_LOADED;
        this.components = NOT_LOADED;
    }

    /**
     * Copying constructor mirrors all fields to decrease memory usage
     * IiDao is responsible for creation of new instances of the fields with new values
     *
     * @param item another item
     */
    CassandraIiImpl(CassandraIiImpl item) {
        this.id = item.id;
        this.meta = item.meta;
        this.parents = item.parents;
        this.components = item.components;
    }

    @Override
    public long getId() {
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
        return (int) id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Ii) {
            Ii local = (Ii) obj;
            return id == local.getId();
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Ii item) {
        if (id == item.getId()) {
            return 0;
        } else {
            return id > item.getId() ? 1 : -1;
        }
    }

    @Override
    public String toString() {
        return String.format("Ii#%d", id);
    }

}
