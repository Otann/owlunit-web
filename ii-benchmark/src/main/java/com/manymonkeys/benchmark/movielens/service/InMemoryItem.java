package com.manymonkeys.benchmark.movielens.service;

import com.manymonkeys.core.ii.InformationItem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class InMemoryItem implements InformationItem, Serializable {

    UUID uuid = UUID.randomUUID();
    Map<String, String> meta = new HashMap<String, String>();
    Map<InformationItem, Double> components = new HashMap<InformationItem, Double>();

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
    public Map<InformationItem, Double> getComponents() {
        return components;
    }

    @Override
    public Double getComponentWeight(InformationItem component) {
        return components.get(component);
    }

    @Override
    public Map<InformationItem, Double> getParents() {
       throw new UnsupportedOperationException();
    }

    @Override
    public Double getParentWeight(InformationItem parent) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({"ConstantConditions"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InMemoryItem that = (InMemoryItem) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Item#");
        sb.append(this.uuid);
        sb.append(" with meta:[");
        for(Iterator<String> iterator = this.meta.keySet().iterator(); iterator.hasNext();) {
            String key = iterator.next();
            sb.append(key);
            sb.append(" -> ");
            sb.append(this.meta.get(key));
            if (iterator.hasNext())
                sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }
}
