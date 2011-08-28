package com.manymonkeys.app.binding;

import com.manymonkeys.core.ii.Ii;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import java.util.Collection;
import java.util.Collections;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class InformationItemItem implements Item {

    public static final Object SINGLE_PROPERTY_ID = InformationItemItem.class;
    private final InformationItemProperty property;

    public InformationItemItem(Ii item) {
        property = new InformationItemProperty(item);
    }

    public Property getItemProperty(Object id) {
        return property;
    }

    public Collection<?> getItemPropertyIds() {
        return Collections.singleton(SINGLE_PROPERTY_ID);
    }

    public boolean addItemProperty(Object id, Property property) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemItem is always read-only");
    }

    public boolean removeItemProperty(Object id) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("InformationItemItem is always read-only");
    }

    @Override
    public String toString() {
        return property.toString();
    }
}
