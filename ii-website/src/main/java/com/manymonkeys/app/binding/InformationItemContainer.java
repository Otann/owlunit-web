package com.manymonkeys.app.binding;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class InformationItemContainer implements Container, Container.Filterable {

    private TagService service;
    private Map<String, UUID> items;
//    private Set<String> filters;

    public InformationItemContainer(TagService service) {
        this.service = service;
        this.items = new HashMap<String, UUID>();
//        this.filters = new HashSet<String>();

        // TODO: Loading *all* tags is extremely heavy, but due to http://notepad.cc/xescide36 there is no other option right now
        // TODO: Create separate index service for name-id pairs and don't load all iis
        for (InformationItem item : service.getAll()) {
            String name = item.getMeta(TagService.NAME);
            if (name != null) {
                items.put(item.getMeta(TagService.NAME), item.getUUID());
            }
        }
    }

//    private void reloadItemsFromService() {
//        if (filters.isEmpty()) {
//            items = Collections.emptyMap();
//        } else {
//            for (String filter : filters) {
//                Map<UUID, String> result = service.searchByMetaPrefix(TagService.NAME, filter);
//                for (Map.Entry<UUID, String> entry : result.entrySet()) {
//                    items.put(entry.getValue(), entry.getKey());
//                }
//            }
//        }
//    }

    /**
     * Gets the Item with the given Item ID from the Container. If the Container
     * does not contain the requested Item, <code>null</code> is returned.
     *
     * @param itemId ID of the Item to retrieve
     * @return the Item with the given ID or <code>null</code> if the Item is
     *         not found in the Container
     */
    public Item getItem(Object itemId) {
        if (service == null) {
            return null;
        }
        UUID id = items.get(itemId.toString());
        if (id == null) {
            return null;
        }
        InformationItem ii = service.getByUUID(id);
        return new InformationItemItem(ii);
    }

    /**
     * Gets the ID's of all Properties stored in the Container. The ID's are
     * returned as a unmodifiable collection.
     *
     * @return unmodifiable collection of Property IDs
     */
    public Collection<?> getContainerPropertyIds() {
        return Collections.singleton(InformationItemItem.SINGLE_PROPERTY_ID);
    }

    /**
     * Gets the ID's of all Items stored in the Container. The ID's are returned
     * as a unmodifiable collection.
     *
     * @return unmodifiable collection of Item IDs
     */
    public Collection<?> getItemIds() {
        return items.keySet();
    }

    /**
     * Gets the Property identified by the given itemId and propertyId from the
     * Container. If the Container does not contain the Property,
     * <code>null</code> is returned.
     *
     * @param itemId     ID of the Item which contains the Property
     * @param propertyId ID of the Property to retrieve
     * @return Property with the given ID or <code>null</code>
     */
    public Property getContainerProperty(Object itemId, Object propertyId) {
        return getItem(itemId).getItemProperty(propertyId);
    }

    /**
     * Gets the data type of all Properties identified by the given Property ID.
     *
     * @param propertyId ID identifying the Properties
     * @return data type of the Properties
     */
    public Class<?> getType(Object propertyId) {
        if (InformationItemItem.SINGLE_PROPERTY_ID.equals(propertyId)) {
            return InformationItem.class;
        } else {
            return null;
        }
    }

    /**
     * Gets the number of Items in the Container.
     *
     * @return number of Items in the Container
     */
    public int size() {
        return items.size();
    }

    /**
     * Tests if the Container contains the specified Item
     *
     * @param itemId ID the of Item to be tested
     * @return boolean indicating if the Container holds the specified Item
     */
    public boolean containsId(Object itemId) {
        return items.containsKey(itemId.toString());
    }

    /**
     * Add a filter for given property.
     * <p/>
     * Only items where given property for which toString() contains or
     * starts with given filterString are visible in the container.
     *
     * @param propertyId      Property for which the filter is applied to.
     * @param filterString    String that must match contents of the property
     * @param ignoreCase      Determine if the casing can be ignored when comparing
     *                        strings.
     * @param onlyMatchPrefix Only match prefixes; no other matches are included.
     */
    public void addContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix) {
        //TODO: should check for InformationItemItem.SINGLE_PROPERTY_ID
//        this.filters.add(filterString);
//        reloadItemsFromService();
    }

    /**
     * Remove all filters from all properties.
     */
    public void removeAllContainerFilters() {
//        this.filters.clear();
//        reloadItemsFromService();
    }

    /**
     * Remove all filters from given property.
     */
    public void removeContainerFilters(Object propertyId) {
//        this.filters.clear();
//        reloadItemsFromService();
    }

    /*

   All methods below are not read-only and so unsupported
   Each method just throws UnsupportedOperationException

    */

    /**
     * Creates a new Item with the given ID into the Container.
     * <p/>
     * <p>
     * The new Item is returned, and it is ready to have its Properties
     * modified. Returns <code>null</code> if the operation fails or the
     * Container already contains a Item with the given ID.
     * </p>
     * <p/>
     * <p>
     * This functionality is optional.
     * </p>
     *
     * @param itemId ID of the Item to be created
     * @return Created new Item, or <code>null</code> in case of a failure
     */
    public Item addItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemContainer is read-only");
    }

    /**
     * Creates a new Item into the Container, and assign it an automatic ID.
     * <p/>
     * <p>
     * The new ID is returned, or <code>null</code> if the operation fails.
     * After a successful call you can use the {@link #getItem(Object ItemId)
     * <code>getItem</code>}method to fetch the Item.
     * </p>
     * <p/>
     * <p>
     * This functionality is optional.
     * </p>
     *
     * @return ID of the newly created Item, or <code>null</code> in case of a
     *         failure
     */
    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemContainer is read-only");
    }

    /**
     * Removes the Item identified by <code>ItemId</code> from the Container.
     * This functionality is optional.
     *
     * @param itemId ID of the Item to remove
     * @return <code>true</code> if the operation succeeded, <code>false</code>
     *         if not
     */
    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemContainer is read-only");
    }

    /**
     * Adds a new Property to all Items in the Container. The Property ID, data
     * type and default value of the new Property are given as parameters.
     * <p/>
     * This functionality is optional.
     *
     * @param propertyId   ID of the Property
     * @param type         Data type of the new Property
     * @param defaultValue The value all created Properties are initialized to
     * @return <code>true</code> if the operation succeeded, <code>false</code>
     *         if not
     */
    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemContainer is read-only");
    }

    /**
     * Removes a Property specified by the given Property ID from the Container.
     * Note that the Property will be removed from all Items in the Container.
     * <p/>
     * This functionality is optional.
     *
     * @param propertyId ID of the Property to remove
     * @return <code>true</code> if the operation succeeded, <code>false</code>
     *         if not
     */
    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemContainer is read-only");
    }

    /**
     * Removes all Items from the Container.
     * <p/>
     * <p>
     * Note that Property ID and type information is preserved. This
     * functionality is optional.
     * </p>
     *
     * @return <code>true</code> if the operation succeeded, <code>false</code>
     *         if not
     */
    public boolean removeAllItems() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemContainer is read-only");
    }
}
