package com.manymonkeys.benchmark.movielens.service;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class InMemoryDao implements InformationItemDao{

    Map<UUID, InformationItem> items = new HashMap<UUID, InformationItem>();
    Map<String, InformationItem> metaIndex = new HashMap<String, InformationItem>();

    private static final String META_FORMAT = "%s#%s";

    @Override
    public InformationItem createInformationItem() {
        InformationItem item = new InMemoryItem();
        items.put(item.getUUID(), item);
        return item;
    }

    @Override
    public void deleteInformationItem(InformationItem item) { }

    @Override
    public Collection<InformationItem> multigetComponents(Collection<InformationItem> items) {
        Collection<InformationItem> result = new LinkedList<InformationItem>();
        for (InformationItem item : items) {
            result.addAll(item.getComponents().keySet());
        }
        return result;
    }

    @Override
    public Collection<InformationItem> multigetParents(Collection<InformationItem> items) {
        Collection<InformationItem> result = new LinkedList<InformationItem>();
        for (InformationItem item : items) {
            result.addAll(item.getParents().keySet());
        }
        return result;
    }

    @Override
    public InformationItem getByUUID(UUID uuid) {
        return items.get(uuid);
    }

    @Override
    public Collection<InformationItem> multigetByUUID(Collection<UUID> uuids) {
        Collection<InformationItem> result = new LinkedList<InformationItem>();
        for (UUID uuid : uuids) {
            result.add(items.get(uuid));
        }
        return result;
    }

    @Override
    public Collection<InformationItem> multigetByMeta(String key, String value) {
        throw new UnsupportedOperationException();
    }

    public InformationItem getByMeta(String key, String value) {
        return metaIndex.get(String.format(META_FORMAT, key, value));
    }

    @Override
    public Map<UUID, String> searchByMetaPrefix(String key, String prefix) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setComponentWeight(InformationItem item, InformationItem component, Double weight) {
        if (item instanceof InMemoryItem && component instanceof InMemoryItem) {
            ((InMemoryItem) item).components.put(component, weight);
        }
    }

    public void addComponentWeight(InformationItem item, InformationItem component, Double apended) {
        if (item instanceof InMemoryItem && component instanceof InMemoryItem) {
            InMemoryItem local = ((InMemoryItem) item);
            Double weight = local.components.get(component);
            if (weight == null) {
                weight = 0D;
            }
            weight += apended;
            local.components.put(component, weight);
        }
    }

    @Override
    public void removeComponent(InformationItem item, InformationItem component) {
        if (item instanceof InMemoryItem && component instanceof InMemoryItem) {
            ((InMemoryItem) item).components.remove(component);
        }
    }

    @Override
    public void setMeta(InformationItem item, String key, String value, boolean indexed) {
        setMeta(item, key, value);
    }

    @Override
    public void setMeta(InformationItem item, String key, String value) {
        if (item instanceof InMemoryItem) {
            ((InMemoryItem) item).meta.put(key, value);
            metaIndex.put(String.format(META_FORMAT, key, value), item);
        }
    }

    @Override
    public void removeMeta(InformationItem item, String key) {
        throw new UnsupportedOperationException();
    }
}
