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
     * Creates new blank Ii object with valid uuid
     *
     * @return InformationItem
     */
    Ii createInformationItem();

    /**
     * Deletes Ii and all links to/from it in datastore
     *
     * @param item to delete
     */
    void deleteInformationItem(Ii item);

    /**
     * Loads Ii from datastore if such Ii exists.
     * Loaded Ii is blank and has only uuid.
     * @see #loadMetadata(java.util.Collection)
     * @see #loadComponents(java.util.Collection)
     * @see #loadParents(java.util.Collection)
     *
     * @param uuid of Ii
     * @return loaded Ii with metadata only
     */
    Ii load(UUID uuid);

    /**
     * Multiget version of {@link #load(java.util.UUID)} method.
     * Queries should be performed in parallel
     * @see #load(java.util.UUID)
     *
     * @param uuids set of uuids of items
     * @return collection of items with metadata only
     */
    Collection<Ii> load(Collection<UUID> uuids);

    /**
     * Loads items that has provided key-value pair in metadata.
     * Loaded items is blank, have only uuid.
     * @see #load(java.util.UUID)
     *
     * @param key   of metadata
     * @param value of metadata
     * @return collection of items with metadata
     */
    Collection<Ii> load(String key, String value);


    /**
     * Updates all metadata for collection of items from datastore and returns as new collection
     * @see #loadComponents(java.util.Collection)
     * @see #loadParents(java.util.Collection)
     *
     * @param items original items
     * @return copy of original items with updated metadata
     */
    Collection<Ii> loadMetadata(Collection<Ii> items);

    /**
     * Updates components for each item in collection
     * Loaded new components are blank (have only uuid)
     * @see #loadMetadata(java.util.Collection)
     * @see #loadParents(java.util.Collection)
     *
     * @param items original items
     * @return copy of original items with updated components
     */
    Collection<Ii> loadComponents(Collection<Ii> items);

    /**
     * Updates parents for each item in collection
     * Loaded new parents are blank (have only uuid)
     * @see #loadMetadata(java.util.Collection)
     * @see #loadComponents(java.util.Collection)
     *
     * @param items original items
     * @return copy of original items with updated parents
     */
    Collection<Ii> loadParents(Collection<Ii> items);

    /**
     * Searches for items that meta's contain word that starts with prefix
     * Items are loaded with metadata only
     *
     * @param key of metadata
     * @param prefix that metadata value should start from
     * @return map of uuids and full meta values
     */
    Map<UUID, String> search(String key, String prefix);

    /**
     * Sets weight of relation between components.
     * If there was no connection, creates one
     *
     * @param item parent Ii
     * @param component child Ii
     * @param weight value of connection
     * @return updated Ii
     */
    Ii setComponentWeight(Ii item, Ii component, Double weight);

    /**
     * Removes relation between items
     *
     * @param item parent ii
     * @param component child ii
     * @return updated Ii
     */
    Ii removeComponent(Ii item, Ii component);

    /**
     * Updates or creates metadata of Ii
     * @see #setUnindexedMeta(Ii, String, String)
     *
     * @param item original item
     * @param key of metadata
     * @param value of metadata
     * @return updated Ii
     */
    Ii setMeta(Ii item, String key, String value);

    /**
     * Updates or creates metadata of Ii
     * This metadata will not be indexed for search through {@link #search(String, String)}
     * @see #setMeta(Ii, String, String)
     *
     * @param item to update
     * @param key of metadata
     * @param value of metadata
     * @return updated Ii
     */
    Ii setUnindexedMeta(Ii item, String key, String value);

    /**
     * Removes metadata from item
     *
     * @param item original item
     * @param key of metadata
     * @return updated item
     */
    Ii removeMeta(Ii item, String key);

}
