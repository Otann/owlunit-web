package com.manymonkeys.app.binding;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.data.util.ObjectProperty;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class InformationItemProperty extends ObjectProperty<InformationItem> {

    public InformationItemProperty(InformationItem item) {
        super(item, InformationItem.class, true);
    }

    public void setReadOnly(boolean newStatus) { }

    @Override
    public String toString() {
        return getValue().getMeta(TagService.NAME);
    }
}
