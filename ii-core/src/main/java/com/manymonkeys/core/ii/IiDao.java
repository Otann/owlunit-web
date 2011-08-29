package com.manymonkeys.core.ii;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public interface IiDao {

    /**
     * Creates new Ii object with valid unique uuid
     *
     * @return InformationItem
     */
    Ii createInformationItem();

    /**
     * Deletes Ii and all links to/from it
     *
     * @param item to delete
     */
    void deleteInformationItem(Ii item);

    /**
     * Loads Ii from datastore with metadata only.
     *
     * @param uuid of Ii
     * @return loaded Ii with metadata only
     */
    Ii loadWithMetadata(UUID uuid);

    /**
     * Multiget version of {@link #loadWithMetadata(java.util.UUID) loadWithMetadata} method.
     * Queries performed in parallel
     *
     * @param uuids set of uuids of items
     * @return collection of items with metadata only
     */
    Collection<Ii> loadWithMetadata(Collection<UUID> uuids);

    /**
     * Reloads all metadata for collection of items from datastore.
     * Supposed to be used with following methods:
     * - {@link #reloadComponents(java.util.Collection) reloadComponents}
     * - {@link #reloadParents(java.util.Collection) reloadParents}
     * @param items
     */
    void reloadMetadata(Collection<Ii> items);

    /**
     * Loads components for set of items as objects with id only and nothing else.
     * You can use {@link #reloadMetadata(java.util.Collection) reloadMetadata} method to load metadata for components.
     * @param items to reload components
     * @return collection of "plain" items with id only that are components of provided items
     */
    Collection<Ii> reloadComponents(Collection<Ii> items);

    /**
     * Loads parents for set of items as objects with id only and nothing else.
     * You can use {@link #reloadMetadata(java.util.Collection) reloadMetadata} method to load metadata for parents.
     * @param items to reload parents
     * @return collection of "plain" items with id only that are parents of provided items
     */
    Collection<Ii> reloadParents(Collection<Ii> items);

    /**
     * Loads items that has provided key-value pair in metadata.
     * @param key   of metadata
     * @param value of metadata
     * @return collection of items with metadata
     */
    Collection<Ii> loadByMeta(String key, String value);

    /**
     * Searches for items that meta's contain word that starts with prefix
     * Items are loaded with metadata only
     *
     * @param key    of meta
     * @param prefix that word should start from
     * @return map of uuids and full meta values
     */
    Map<UUID, String> searchByMetaPrefix(String key, String prefix);

    /**
     * Sets weight of relation between components.
     * If there was no connection, creates one
     *
     * @param item      - parent ii
     * @param component - child ii
     * @param weight    of connection
     */
    void setComponentWeight(Ii item, Ii component, Double weight);

    /**
     * Removes relation between items
     *
     * @param item      parent ii
     * @param component child ii
     */
    void removeComponent(Ii item, Ii component);

    /**
     * Updates or creates metadata of Ii
     * Does not create index for this pair
     *
     * @param item  to update
     * @param key   of metadata
     * @param value of metadata
     */
    void setMeta(Ii item, String key, String value);

    /**
     * Updates or creates metadata of Ii
     * Allows to index this key for this item
     *
     * @param item   to update
     * @param key    of metadata
     * @param value  of metadata
     * @param indexed enables meta for searchByPrefix
     */
    void setMeta(Ii item, String key, String value, boolean indexed);

    /**
     * Removes metadata from item
     *
     * @param item to operate
     * @param key  of metadata
     */
    void removeMeta(Ii item, String key);

}
