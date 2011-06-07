package com.manymonkeys.ui;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import org.vaadin.tokenfield.TokenField;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class TagTokenField extends TokenField {

    private Set<InformationItem> items;

    public TagTokenField(Container container) {
        this.items = new HashSet<InformationItem>();
        super.setTokenInsertPosition(InsertPosition.BEFORE);
        super.setContainerDataSource(container);
    }

    public Collection<InformationItem> getInformationItems() {
        return items;
    }

    private InformationItem extractInformationItem(Item item) {
        return (InformationItem) item.getItemProperty(null).getValue();
    }

    @Override
    protected void onTokenInput(Object tokenId) {
        Item item = getContainerDataSource().getItem(tokenId);
        if (item == null)
            return;

        InformationItem ii = extractInformationItem(getContainerDataSource().getItem(tokenId));
        items.add(ii);

        String name = ii.getMeta(TagService.NAME);
        if (name == null)
            name = ii.getUUID().toString();

        super.onTokenInput(item);
    }

    @Override
    protected void onTokenClick(Object tokenId) {
        Item item = getContainerDataSource().getItem(tokenId);
        if (item == null)
            return;

        InformationItem ii = extractInformationItem(getContainerDataSource().getItem(tokenId));
        items.remove(ii);
        super.onTokenClick(item);

    }

    @Override
    protected void onTokenDelete(Object tokenId) {
        Item item = getContainerDataSource().getItem(tokenId);
        if (item == null)
            return;

        InformationItem ii = extractInformationItem(getContainerDataSource().getItem(tokenId));
        items.remove(ii);
        super.onTokenDelete(item);
    }

    @Override
    protected void configureTokenButton(Object tokenId, Button button) {
        super.configureTokenButton(tokenId, button);
        button.setCaption(getTokenCaption(tokenId));
    }
}
