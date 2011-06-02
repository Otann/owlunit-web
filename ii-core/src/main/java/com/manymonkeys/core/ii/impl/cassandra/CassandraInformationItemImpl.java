package com.manymonkeys.core.ii.impl.cassandra;

import com.google.common.collect.ImmutableMap;
import com.manymonkeys.core.ii.InformationItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraInformationItemImpl implements InformationItem, Comparable<InformationItem> {

    UUID uuid;
    Map<String, String> meta;
    Map<InformationItem, Double> components;
    Map<InformationItem, Double> parents;

    CassandraInformationItemImpl(UUID uuid) {
        this.uuid = uuid;
        this.meta = new HashMap<String, String>();
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Map<String, String> getMetaMap() {
        return ImmutableMap.copyOf(meta);
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
        return parents;
    }

    @Override
    public Double getParentWeight(InformationItem parent) {
        return parents.get(parent);
    }


    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InformationItem) {
            InformationItem local = (InformationItem) obj;
            return uuid.equals(local.getUUID());
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(InformationItem item) {
        return uuid.compareTo(item.getUUID());
    }

    @Override
    public String toString() {
        return uuid.toString();
    }
}
