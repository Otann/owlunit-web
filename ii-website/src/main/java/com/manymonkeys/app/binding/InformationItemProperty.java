package com.manymonkeys.app.binding;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.data.util.ObjectProperty;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class InformationItemProperty extends ObjectProperty<Ii> {

    public InformationItemProperty(Ii item) {
        super(item, Ii.class, true);
    }

    public void setReadOnly(boolean newStatus) { }

    @Override
    public String toString() {
        if (getValue().getMeta(TagService.NAME) != null)
            return getValue().getMeta(TagService.NAME);
        else
            return getValue().getUUID().toString();
    }
}
