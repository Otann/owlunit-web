package com.manymonkeys.app.page;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.PersonService;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.ui.ItemTag;
import com.manymonkeys.ui.theme.Stream;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.Page;
import org.vaadin.navigator7.uri.Param;

import java.util.*;

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
        sb.append(String.format("<b>%d</b> has this item as a component", item.getParents().size()));
        techLabel.setValue(sb.toString());
        techLabel.setContentMode(Label.CONTENT_XHTML);
    }

    public void refillComponents() {
        if (this.item == null) {
            return;
        }

        components.removeAllComponents();

        int uselessCount = 0;
        Map<InformationItem, Double> componentsMap = new HashMap<InformationItem, Double>();

        for (Map.Entry<InformationItem, Double> componentEntry : item.getComponents().entrySet()) {
            if ((componentEntry.getValue() > 0) && (componentEntry.getValue() < 1.99)) {
                ++uselessCount;
            } else {
                componentsMap.put(componentEntry.getKey(), componentEntry.getValue());
            }
        }

        for (Map.Entry<InformationItem, Double> componentEntry : sortByValue(componentsMap, false).entrySet()) {
            InformationItem item = componentEntry.getKey();
            ItemTag tag = new ItemTag(item, componentEntry.getValue(), ItemPage.class);
            tag.setWidth(null);

            if (item.getMeta("CREATED BY").equals(PersonService.class.getName())) {
                tag.addStyleName("ii-actor");
            }

            components.addComponent(tag);
        }

        if (uselessCount > 0) {
            Label uselessCountLabel = new Label(String.format("and %d other tags with low weight", uselessCount));
            uselessCountLabel.addStyleName("ii-useless-tag-counter");
            uselessCountLabel.setWidth(null);
            components.addComponent(uselessCountLabel);
        }


    }

    public void refillStream() {
        if (this.item == null) {
            return;
        }

        stream.removeAllComponents();

        Button loader = new Button("Load Recommendations");
        loader.setWidth("100%");
        stream.addComponent(loader);
        loader.addListener(new Button.ClickListener(){
            @Override
            public void buttonClick(Button.ClickEvent event) {
                stream.removeAllComponents();

                long startTime = System.currentTimeMillis();

                long limit = STREAM_SIZE_LIMIT;
                Map<InformationItem, Double> recommendations = recommender.getMostLike(item, service);
                service.multigetComponents(recommendations.keySet());

                for (Map.Entry<InformationItem, Double> recommendation : recommendations.entrySet()) {
                    if (limit-- < 0)
                        break;

                    InformationItem item = recommendation.getKey();
                    Double value = recommendation.getValue();
                    ItemTag tag = new ItemTag(item, value, ItemTag.DEFAULT_COMPONENTS_LIMIT, ItemPage.class);
                    stream.addComponent(tag);
                }
            }
        });

    }

    private static<K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map, final boolean straight) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return straight ?
                        o1.getValue().compareTo(o2.getValue()) :
                        o2.getValue().compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
