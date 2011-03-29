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
     * Creates new InformationItem object with valid unique uuid
     *
     * @return InformationItem
     */
    InformationItem createInformationItem();

    /**
     * Deletes InformationItem and all links to/from it
     *
     * @param item to delete
     */
    void deleteInformationItem(InformationItem item);

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
     * Faster than sequential getByUUID
     *
     * @param uuids set of uuids of items
     * @return Set of Items
     */
    Collection<InformationItem> multigetByUUID(Collection<UUID> uuids);

    /**
     * Loads items that has mey-value pair in metadata
     *
     * @param key   of metadata
     * @param value of metadata
     * @return collection of items
     */
    Collection<InformationItem> multigetByMeta(String key, String value);

    /**
     * Sets weight of relation between components.
     * Tf there was no connection, creates one
     *
     * @param item      - parent ii
     * @param component - child ii
     * @param weight    of connection
     */
    void setComponentWeight(InformationItem item, InformationItem component, Double weight);

    /**
     * Removes relation between items
     *
     * @param item      parent ii
     * @param component child ii
     */
    void removeComponent(InformationItem item, InformationItem component);

    /**
     * Updates or creates metadata of InformationItem
     *
     * @param item  to update
     * @param key   of metadata
     * @param value of metadata
     */
    void setMeta(InformationItem item, String key, String value);

    /**
     * Removes metadata from item
     *
     * @param item to operate
     * @param key  of metadata
     */
    void removeMeta(InformationItem item, String key);


}
