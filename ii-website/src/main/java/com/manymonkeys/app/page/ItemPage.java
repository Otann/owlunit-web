package com.manymonkeys.app.page;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.ui.ItemTag;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Page;
import org.vaadin.navigator7.uri.Param;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Page
@Configurable(preConstruction = true)
public class ItemPage extends CustomLayout {

    public static final int STREAM_SIZE_LIMIT = 20;

    @Autowired
    private TagService service;

    @Autowired
    private Recommender recommender;

    @Param(pos = 0)
    String uuid;

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
        setItemId(uuid);
    }

    public void setItemId(String id) {
        if (id == null) {
            setItem(null);
        } else {
            UUID uuid = UUID.fromString(id);
            setItem(service.getByUUID(uuid));
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

        Label techCaption = new Label("Tech Info");
        techCaption.setStyleName(Stream.LABEL_H2);
        techCaption.setSizeUndefined();
        meta.addComponent(techCaption);

        Label techLabel = new Label();
        techLabel.setSizeUndefined();
        meta.addComponent(techLabel);
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : item.getMetaMap().entrySet()) {
            sb.append(String.format("%s : %s<br>", entry.getKey(), entry.getValue()));
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
        Iterator<Map.Entry<InformationItem, Double>> iterator = item.getComponents().entrySet().iterator();
        while (iterator.hasNext() && limit > 0) {
            limit--;
            Map.Entry<InformationItem, Double> componentEntry = iterator.next();
            if (componentEntry.getValue() > 0) {
                ItemTag tag = new ItemTag(componentEntry.getKey(), componentEntry.getValue(), ItemPage.class);
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
        Map<InformationItem, Double> recommendations = recommender.getMostLike(item, service);
        service.multigetComponents(recommendations.keySet());
        Iterator<Map.Entry<InformationItem, Double>> iterator = recommendations.entrySet().iterator();
        while (iterator.hasNext() && limit > 0) {
            limit--;
            Map.Entry<InformationItem, Double> recommendation = iterator.next();
            InformationItem item = recommendation.getKey();
            Double value = recommendation.getValue();
            ItemTag tag = new ItemTag(item, value, ItemTag.DEFAULT_COMPONENTS_LIMIT, ItemPage.class);
            stream.addComponent(tag);
        }

    }

}
