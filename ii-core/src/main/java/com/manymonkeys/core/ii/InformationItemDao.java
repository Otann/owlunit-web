package com.manymonkeys.core.ii;

import java.util.Collection;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public interface InformationItemDao {

    /**
     * Creates new InformationItem object with valid unique id/uuid
     *
     * @return InformationItem
     */
    InformationItem createInformationItem();

    /**
     * Performs quick load of components map for set of items.
     * Also reloads components for each item
     *
     * @param items to load components
     * @return collection of components
     */
    Collection<InformationItem> multigetComponents(Collection<InformationItem> items);

    /**
     * Performs quick load of parents map for set of items.
     * Also reloads components for each item
     *
     * @param items to load components
     * @return collection of parents
     */
    Collection<InformationItem> multigetParents(Collection<InformationItem> items);

    /**
     * Loads InformationItem from datastore.
     *
     * @param uuid - set of InformationItem
     * @return loaded InformationItem
     */
    InformationItem getByUUID(UUID uuid);

    /**
     * Multiget version of {@see getByUUID(UUID uuid)}
     *
     * @param uuids set of uuids of items
     * @return Set of Items
     */
    Collection<InformationItem> multigetByUUID(Collection<UUID> uuids);

    /**
     * Loads items that has mey-value pair in metadata
     *
     * @param key   for metadata
     * @param value of metadata
     * @return collection of items
     */
    Collection<InformationItem> multigetByMeta(String key, String value);

    void setComponentWeight(InformationItem item, InformationItem component, Double weight);

    void removeComponent(InformationItem item, InformationItem component);

    void setMeta(InformationItem item, String key, String value);

    void removeMeta(InformationItem item, String key);

}
