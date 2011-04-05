package com.manymonkeys.app.page;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.spring.SpringContextHelper;
import com.manymonkeys.ui.component.ItemTag;
import com.manymonkeys.ui.component.TagTokenField;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.data.Property;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Page;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

        //TODO: fix this after @Autowired gets fixed
//        SpringContextHelper helper = new SpringContextHelper(getApplication());
//        service = (TagService) helper.getBean("iiService");
//        recommender = (Recommender) helper.getBean("iiRecommender");

        setStyleName("ii-search-page");

        CssLayout suggestions = new CssLayout();
        suggestions.setWidth("100%");
        searchTokens = new TagTokenField(service);
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

        Map<InformationItem, Double> query = new HashMap<InformationItem, Double>();
        Collection<InformationItem> items = searchTokens.getInformationItems();
        for (InformationItem item : items) {
            // Add Item itself
            Double weight = query.get(item);
            if (weight == null) {
                weight = 0D;
            }
            weight += 1;
            query.put(item, 1D);

            // add all components
            for (Map.Entry<InformationItem, Double> component : item.getComponents().entrySet()) {
                Double componentWeight = query.get(component.getKey());
                if (componentWeight == null) {
                    componentWeight = 0D;
                }
                componentWeight += component.getValue();
                query.put(component.getKey(), componentWeight);
            }
        }
        Map<InformationItem, Double> result = recommender.getMostLike(query, service);
        for (InformationItem item : items) {
            String name = item.getMeta(TagService.NAME);
            result.remove(item);
        }
        long limit = SEARCH_RESULTS_LIMIT;
        Iterator<Map.Entry<InformationItem, Double>> it = result.entrySet().iterator();
        while (it.hasNext() && limit > 0) {
            --limit;
            Map.Entry<InformationItem, Double> item = it.next();

            ItemTag tag = new ItemTag(item.getKey(), item.getValue(), ItemTag.COMPONENTS_LIMIT);
            searchResults.addComponent(tag);
        }

        long stopTime = System.currentTimeMillis();
//        VConsole.log("Search results generated in time: " + (stopTime - startTime));

    }
}

