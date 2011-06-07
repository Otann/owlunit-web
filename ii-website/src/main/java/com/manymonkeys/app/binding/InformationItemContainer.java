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
    private Collection<Item> itemNames = new HashSet<Item>();

    public InformationItemContainer(TagService service) {
        this.service = service;
    }

    private Item loadItemFromService(Object itemId) {
        if (itemId instanceof InformationItemItem)
            return (InformationItemItem) itemId;

        if (itemId instanceof UUID)
            return new InformationItemItem(service.loadByUUID((UUID) itemId));

        UUID id = null;
        try {
            id = UUID.fromString((String) itemId);
        } catch (Exception e) { }

        InformationItem ii = null;
        if (id != null) {
            ii = service.loadByUUID(id);
        } else if (itemId instanceof String) {
            ii = service.getTag((String) itemId);
        }

        if (ii == null)
            return null;
        else
            return new InformationItemItem(ii);
    }

    public Item getItem(Object itemId) {
        return loadItemFromService(itemId);
    }

    public Collection<?> getContainerPropertyIds() {
        return Collections.singleton(InformationItemItem.SINGLE_PROPERTY_ID);
    }

    public Collection<?> getItemIds() {
        return itemNames;
    }

    public Property getContainerProperty(Object itemId, Object propertyId) {
        return getItem(itemId).getItemProperty(propertyId);
    }

    public Class<?> getType(Object propertyId) {
        return InformationItem.class;
    }

    public int size() {
        return itemNames.size();
    }

    public boolean containsId(Object itemId) {
        return loadItemFromService(itemId) != null;
    }

    public void addContainerFilter(Object propertyId, String filterString, boolean ignoreCase, boolean onlyMatchPrefix) { }

    public void removeAllContainerFilters() { }

    public void removeContainerFilters(Object propertyId) { }

    public Item addItem(Object itemId) throws UnsupportedOperationException {
        Item item = loadItemFromService(itemId);
        itemNames.add(item);
        return item;
    }

    public Object addItem() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Cant add empty item");
    }

    public boolean removeItem(Object itemId) throws UnsupportedOperationException {
        return false;
    }

    public boolean addContainerProperty(Object propertyId, Class<?> type, Object defaultValue) throws UnsupportedOperationException {
        return false;
    }

    public boolean removeContainerProperty(Object propertyId) throws UnsupportedOperationException {
        return false;
    }

    public boolean removeAllItems() throws UnsupportedOperationException {
        return false;
    }
}
