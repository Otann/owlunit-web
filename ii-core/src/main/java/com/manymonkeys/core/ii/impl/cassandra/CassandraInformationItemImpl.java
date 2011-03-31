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

    // Tech
    CassandraInformationItemDaoImpl dao;

    // Raw
    UUID uuid;
    Map<String, String> meta;   // always fetched from db

    Map<InformationItem, Double> components; // lazy or with multiget
    Map<InformationItem, Double> parents;    // lazy or with multiget

    CassandraInformationItemImpl(UUID uuid, CassandraInformationItemDaoImpl dao) {
        // hide construction to package-level
        this.uuid = uuid;
        this.dao = dao;
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
        if (components == null) {
            dao.reloadComponents(this); //TODO: review
        }
        return ImmutableMap.copyOf(components);
    }

    @Override
    public Double getComponentWeight(InformationItem component) {
        if (components == null) {
            dao.reloadComponents(this); //TODO: review
        }
        return components.get(component);
    }

    @Override
    public Map<InformationItem, Double> getParents() {
        if (parents == null) {
            dao.reloadParents(this); //TODO: review
        }
        return null;
    }

    @Override
    public Double getParentWeight(InformationItem parent) {
        if (parents == null) {
            dao.reloadParents(this); //TODO: review
        }
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
}
