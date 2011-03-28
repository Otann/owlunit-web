package com.manymonkeys.ui.component;

import com.manymonkeys.app.binding.InformationItemContainer;
import com.manymonkeys.app.binding.InformationItemItem;
import com.manymonkeys.core.ii.impl.neo4j.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.ui.theme.Stream;
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

    public TagTokenField(TagService service) {
        items = new HashSet<InformationItem>();

        setStyleName(Stream.TOKEN_BOX);
        addStyleName(Stream.TOKEN_BOX_TEXTFIELD);


        super.setTokenInsertPosition(InsertPosition.BEFORE);
        super.setContainerDataSource(new InformationItemContainer(service));
    }

    public Collection<InformationItem> getInformationItems() {
        return items;
    }

    private InformationItem extractInformationItem(Object tokenId) {
        return (InformationItem) getContainerDataSource().getItem(tokenId)
                .getItemProperty(InformationItemItem.SINGLE_PROPERTY_ID).getValue();
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
