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

    public Map<UUID, InformationItem> items = new HashMap<UUID, InformationItem>();
    public Map<String, InformationItem> metaIndex = new HashMap<String, InformationItem>();

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
    public InformationItem loadByUUID(UUID uuid) {
        return items.get(uuid);
    }

    @Override
    public Collection<InformationItem> loadByUUIDs(Collection<UUID> uuids) {
        Set<InformationItem> result = new HashSet<InformationItem>();
        for (UUID uuid : uuids)
            result.add(items.get(uuid));
        return result;
    }

    @Override
    public void reloadMetadata(Collection<InformationItem> items) { }

    @Override
    public Collection<InformationItem> reloadComponents(Collection<InformationItem> items) {
        Set<InformationItem> result = new HashSet<InformationItem>();
        for (InformationItem item : items)
            result.addAll(item.getComponents().keySet());
        return result;
    }

    @Override
    public Collection<InformationItem> reloadParents(Collection<InformationItem> items) {
        Set<InformationItem> result = new HashSet<InformationItem>();
        for (InformationItem item : items)
            result.addAll(item.getParents().keySet());
        return result;
    }

    @Override
    public Collection<InformationItem> loadByMeta(String key, String value) {
        InformationItem item = metaIndex.get(String.format(META_FORMAT, key, value));
        if (item == null)
            return Collections.emptySet();
        else
            return Collections.singleton(item);
    }

    @Override
    public Map<UUID, String> searchByMetaPrefix(String key, String prefix) {
        return null;
    }

    @Override
    public void setComponentWeight(InformationItem item, InformationItem component, Double weight) {
        item.getComponents().put(component, weight);
    }

    @Override
    public void removeComponent(InformationItem item, InformationItem component) {
        item.getComponents().remove(component);
    }

    @Override
    public void setMeta(InformationItem item, String key, String value) {
        setMeta(item, key, value, true);
    }

    @Override
    public void setMeta(InformationItem item, String key, String value, boolean indexed) {
        item.getMetaMap().put(key, value);
        metaIndex.put(String.format(META_FORMAT, key, value), item);
    }

    @Override
    public void removeMeta(InformationItem item, String key) { }
}
