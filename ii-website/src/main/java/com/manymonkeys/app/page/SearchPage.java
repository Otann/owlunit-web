package com.manymonkeys.app.page;

import com.manymonkeys.app.binding.InformationItemContainer;
import com.manymonkeys.app.binding.InformationItemItem;
import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.ui.ItemTag;
import com.manymonkeys.ui.TagTokenField;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.data.Property;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Page;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
@Configurable(preConstruction = true)
public class SearchPage extends VerDashLayout implements Button.ClickListener {

    private static final long SEARCH_RESULTS_LIMIT = 200;

    @Autowired
    TagService service;

    @Autowired
    Recommender recommender;

    TagTokenField searchTokens;
    Layout searchResults;


    @Override
    public void attach() {
        super.attach();

        setStyleName("ii-search-page");

        CssLayout suggestions = new CssLayout();
        suggestions.setWidth("100%");

        searchTokens = new TagTokenField(service, new InformationItemContainer(service), InformationItemItem.SINGLE_PROPERTY_ID);
        searchTokens.setStyleName(Stream.TOKEN_BOX);
        searchTokens.addStyleName(Stream.TOKEN_BOX_TEXTFIELD);
        searchTokens.setInputPrompt("Add Search Keywords");
        searchTokens.setNewTokensAllowed(false);
        searchTokens.addListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent event) {
                searchResults.removeAllComponents();
            }
        });
        addComponent(searchTokens);

        Button searchButton = new Button("Get Something Like That");
        searchButton.setStyleName(Stream.BUTTON_LINK);
        searchButton.addStyleName(Stream.BUTTON_SEARCH);
        searchButton.addListener(this);
        addComponent(searchButton);

        searchResults = new VerDashLayout();
        searchResults.addStyleName(Stream.SEARCH_PAGE_RESULTS_BOX);
        searchResults.setMargin(true);
        addComponent(searchResults);
    }

    public void buttonClick(Button.ClickEvent event) {
        long startTime = System.currentTimeMillis();

        Map<InformationItem, Double> queryMap = new HashMap<InformationItem, Double>();
        Collection<InformationItem> queryItems = searchTokens.getInformationItems();

        service.reloadComponents(queryItems);
        for (InformationItem queryItem : queryItems) {
            // Add Item itself
            Double weight = queryMap.get(queryItem);
            if (weight == null) {
                weight = 0D;
            }
            weight += 1;
            queryMap.put(queryItem, 1D);

            // add all components
            for (Map.Entry<InformationItem, Double> component : queryItem.getComponents().entrySet()) {
                Double componentWeight = queryMap.get(component.getKey());
                if (componentWeight == null) {
                    componentWeight = 0D;
                }
                componentWeight += component.getValue();
                queryMap.put(component.getKey(), componentWeight);
            }
        }

        Map<InformationItem, Double> result = recommender.getMostLike(queryMap, service);
        for (InformationItem item : queryItems) {
            result.remove(item);
        }

        long limit = SEARCH_RESULTS_LIMIT;
        List<InformationItem> reloadList = new LinkedList<InformationItem>();
        List<ItemTag> displayTags = new LinkedList<ItemTag>();

        for (Map.Entry<InformationItem, Double> entry : result.entrySet()) {
            if (limit-- <= 0)
                break;

            ItemTag tag = new ItemTag(entry.getKey(), entry.getValue(), ItemTag.DEFAULT_COMPONENTS_LIMIT, ItemPage.class);
            reloadList.add(entry.getKey());
            reloadList.addAll(tag.getDisplayedComponents());
            displayTags.add(tag);
        }

        service.reloadMetadata(reloadList);
        for(ItemTag tag : displayTags)
            searchResults.addComponent(tag);

        getApplication().getMainWindow().showNotification(String.format("Search took %d seconds", (System.currentTimeMillis() - startTime) / 1000));

    }
}

