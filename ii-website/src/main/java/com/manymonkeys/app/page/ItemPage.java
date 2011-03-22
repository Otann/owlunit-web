package com.manymonkeys.app.page;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.impl.neo4j.Neo4jInformationItemImpl;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.spring.SpringContextHelper;
import com.manymonkeys.ui.component.ItemTag;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.navigator7.Page;
import org.vaadin.navigator7.uri.Param;

import java.util.Iterator;
import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
public class ItemPage extends CustomLayout {

    public static final int STREAM_SIZE_LIMIT = 20;

    @Autowired
    private TagService service;

    @Autowired
    private Recommender recommender;

    @Param(pos = 0)
    Long id;

    private InformationItem item;

    private Layout meta;
    private Layout components;
    private Layout stream;

    public ItemPage() {
        super("ii_page");
    }

    public InformationItem getItem() {
        return item;
    }

    public TagService getService() {
        return service;
    }

    public Recommender getRecommender() {
        return recommender;
    }

    private void addComponents() {
        removeAllComponents();
        if (item == null) {

            Label label = new Label("No ii loaded, sorry.");
            label.addStyleName(Stream.ERROR_MESSAGE);
            super.addComponent(label);

        } else {

            // Layout for meta-information
            meta = new CssLayout();
            meta.setSizeUndefined();
            super.addComponent(meta, "ii-meta");

            // Layout for components
            components = new CssLayout();
            super.addComponent(components, "ii-components");

            // Layout for stream
            stream = new CssLayout();
            super.addComponent(stream, "ii-stream");
        }
    }


    @Override
    public void attach() {
        super.attach();

        //TODO: fix this after @Autowired gets fixed
        SpringContextHelper helper = new SpringContextHelper(getApplication());
        service = (TagService) helper.getBean("iiService");
        recommender = (Recommender) helper.getBean("iiRecommender");

        setItemId(id);
    }

    public void setItemId(Long id) {
        if (id == null) {
            setItem(null);
        } else {
            setItem(service.getById(id));
        }
    }

    public void setItem(InformationItem item) {
        if (this.item != item) {
            this.item = item;

            addComponents();

            if (item != null) {
                refillAll();
            }
        }
    }

    public void refillAll() {
        refillMeta();
        refillComponents();
        refillStream();
    }

    public void refillMeta() {
        if (this.item == null) {
            return;
        }

        meta.removeAllComponents();

        Label name = new Label(item.getMeta(TagService.NAME));
        name.setWidth(null);
        name.addStyleName(Stream.ITEM_PAGE_NAME);
        meta.addComponent(name);

        String itemType = item.getMeta(Neo4jInformationItemImpl.ITEM_CLASS_NAME);

        Label techCaption = new Label("Tech Info");
        techCaption.setStyleName(Stream.LABEL_H2);
        techCaption.setSizeUndefined();
        meta.addComponent(techCaption);

        Label techLabel = new Label();
        techLabel.setSizeUndefined();
        meta.addComponent(techLabel);
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Type of current item: %s<br>", itemType));
        sb.append(String.format("Type of item creator: %s<br>", item.getMeta(Neo4jInformationItemImpl.DAO_CLASS_NAME)));

        if (MovieService.MOVIE_CLASS_NAME.equals(itemType)) {
            sb.append("<br>Add some description here, links to IMDB/Wikipedia and cover image.<br>");
            sb.append("Alternative title. Grabbed IMDB/RottenTomatoes rating.<br>");
            sb.append("Friends that have this item too.");
        }

        techLabel.setValue(sb.toString());
        techLabel.setContentMode(Label.CONTENT_XHTML);
    }

    public void refillComponents() {
        if (this.item == null) {
            return;
        }

        components.removeAllComponents();

        int limit = 1000;
        Iterator<Map.Entry<InformationItem, Double>> iterator = item.getComponentsWeights();
        while (iterator.hasNext() && limit > 0) {
            limit--;
            Map.Entry<InformationItem, Double> componentEntry = iterator.next();
            if (componentEntry.getValue() > 0) {
                ItemTag tag = new ItemTag(componentEntry.getKey(), componentEntry.getValue());
                tag.setWidth(null);
                components.addComponent(tag);
            }
        }
    }

    public void refillStream() {
        if (this.item == null) {
            return;
        }

        stream.removeAllComponents();

        long startTime = System.currentTimeMillis();

        long limit = STREAM_SIZE_LIMIT;
        Iterator<Map.Entry<InformationItem, Double>> iterator = recommender.getMostLike(item).entrySet().iterator();
        while (iterator.hasNext() && limit > 0) {
            limit--;
            Map.Entry<InformationItem, Double> recommendation = iterator.next();
            InformationItem item = recommendation.getKey();
            Double value = recommendation.getValue();
            ItemTag tag = new ItemTag(item, value, ItemTag.COMPONENTS_LIMIT);
            stream.addComponent(tag);
        }

        long stopTime = System.currentTimeMillis();
//        VConsole.log("Similar items generation took time: " + (stopTime - startTime));
    }

    /*
    Safety Overrides
     */

    @Override
    public void addComponent(Component c, String location) {
        throw new UnsupportedOperationException("You do not add to PageLayout");
    }

    @Override
    public void addComponent(Component c) {
        throw new UnsupportedOperationException("You do not add to PageLayout");
    }
}
