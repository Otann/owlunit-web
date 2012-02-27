package com.owlunit.core.ii;

import java.util.Collection;
import java.util.Map;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public interface IiDao {

    ////////////////////////////////////////////////
    ////////////////    Create / Delete / Load
    ////////////////////////////////////////////////

    /**
     * Creates new blank Ii object with valid id
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
     * Loaded Ii is blank and has only id.
     * @see #loadMeta(java.util.Collection)
     * @see #loadComponents(java.util.Collection)
     * @see #loadParents(java.util.Collection)
     *
     * @param id of Ii
     * @return loaded Ii with metadata only
     */
    Ii load(long id);

    /**
     * Multiget version of {@link #load(long)} method.
     * Queries should be performed in parallel
     * @see #load(long)
     *
     * @param ids set of ids of items
     * @return collection of items with metadata only
     */
    Collection<Ii> load(Collection<Long> ids);

    ////////////////////////////////////////////////
    ////////////////    Meta
    ////////////////////////////////////////////////

    /**
     * Updates or creates metadata of Ii
     * @see #setMetaUnindexed(Ii, String, String)
     *
     * @param item original item
     * @param key of metadata
     * @param value of metadata
     * @return updated Ii
     */
    Ii setMeta(Ii item, String key, String value);

    /**
     * Loads items that has provided key-value pair in metadata.
     * Loaded items is blank, have only id.
     * @see #load(long)
     *
     * @param key   of metadata
     * @param value of metadata
     * @return collection of blank items
     */
    Collection<Ii> load(String key, String value);

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
    Ii setMetaUnindexed(Ii item, String key, String value);

    /**
     * Updates metadata for item
     * @see #loadComponents(Ii)
     * @see #loadParents(Ii)
     *
     * @param item original item
     * @return copy of original item with updated metadata
     */
    Ii loadMeta(Ii item);

    /**
     * Collection-based version of {@link #loadMeta(Ii)}
     * Queries performed in parallel
     *
     * @param items original items
     * @return copy of original items with updated metadata
     */
    Collection<Ii> loadMeta(Collection<Ii> items);

    /**
     * Removes metadata from item
     *
     * @param item original item
     * @param key of metadata
     * @return updated item
     */
    Ii removeMeta(Ii item, String key);

    ////////////////////////////////////////////////
    ////////////////    Tree operations
    ////////////////////////////////////////////////

    /**
     * Updates components links for item.
     * New components loaded blank, with id only, old components are same as original
     * @see #loadMeta(Ii)
     * @see #loadParents(Ii)
     *
     * @param item original item
     * @return copy of original item with updated components links
     */
    Ii loadComponents(Ii item);

    /**
     * Collection-based version of {@link #loadComponents(Ii)}
     * Queries performed in parallel
     *
     * @param items original items
     * @return copy of original items with updated components
     */
    Collection<Ii> loadComponents(Collection<Ii> items);

    /**
     * Updates parents links for item.
     * New parents loaded blank, with id only, old parents are same as original
     * @see #loadMeta(Ii)
     * @see #loadComponents(Ii)
     *
     * @param item original item
     * @return copy of original item with updated parents links
     */
    Ii loadParents(Ii item);

    /**
     * Collection-based version of {@link #loadParents(Ii)}
     * Queries performed in parallel
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
     * @return map of ids and full meta values
     */
    Collection<Ii> search(String key, String prefix);

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
     * Loads subtree three-level deep with indirect weights
     *
     * @param item root item of subtree
     * @return map of subtree items with indirect weights
     */
    Map<Ii, Double> getIndirectComponents(Ii item);

}
