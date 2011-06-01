package com.manymonkeys.ui;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.data.Container;
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
    private Object properyId;

    public TagTokenField(TagService service, Container container, Object propertyId) {
        this.items = new HashSet<InformationItem>();
        this.properyId = properyId;

        super.setTokenInsertPosition(InsertPosition.BEFORE);
        super.setContainerDataSource(container);
    }

    public Collection<InformationItem> getInformationItems() {
        return items;
    }

    private InformationItem extractInformationItem(Object tokenId) {
        return (InformationItem) getContainerDataSource().getItem(tokenId).getItemProperty(properyId).getValue();
    }

    @Override
    protected void onTokenInput(Object tokenId) {
        super.onTokenInput(tokenId);
        items.add(extractInformationItem(tokenId));
    }

    @Override
    protected void onTokenClick(Object tokenId) {
        super.onTokenClick(tokenId);
        items.add(extractInformationItem(tokenId));
    }

    @Override
    protected void onTokenDelete(Object tokenId) {
        super.onTokenDelete(tokenId);
        items.add(extractInformationItem(tokenId));
    }

    @Override
    protected void configureTokenButton(Object tokenId, Button button) {
        super.configureTokenButton(tokenId, button);
        button.setCaption(getTokenCaption(tokenId));
    }
}
