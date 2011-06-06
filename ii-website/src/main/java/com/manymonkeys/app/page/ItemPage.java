package com.manymonkeys.app.page;

import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
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
    public static final double COMPONENT_THRESHOLD = 2.1;

    @Autowired
    TagService service;

    @Autowired
    Recommender recommender;

    @Param(pos = 0)
    String uuid;

    InformationItem item;

    Layout meta;
    Layout components;
    Layout stream;

    public ItemPage() {
        super("ii_page");
    }

    public InformationItem getItem() {
        return item;
    }

    public TagService getService() {
        return service;
    }

    private void reloadPageLayout() {
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
        try {
            item = service.loadByUUID(UUID.fromString(id));
        } catch (Exception e) {
            item = null;
        }

        reloadPageLayout();

        if (item != null) {
            service.reloadParents(Collections.singleton(item));
            service.reloadComponents(Collections.singleton(item));
            refillAll();
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

        Label nameLabel = new Label(item.getMeta(TagService.NAME));
        nameLabel.setWidth(null);
        nameLabel.addStyleName(Stream.ITEM_PAGE_NAME);
        meta.addComponent(nameLabel);

        Label infoLabel = new Label();
        infoLabel.setSizeUndefined();
        meta.addComponent(infoLabel);
        StringBuffer sb = new StringBuffer();

        sb.append(String.format("<b>%d</b> has this item as a component</br>", item.getParents().size()));
        sb.append(String.format("This item's id is <b>%s</b></br>", item.getUUID().toString()));

        if (item.getMeta(MovieService.CLASS_MARK_KEY).equals(MovieService.CLASS_MARK_VALUE)) {
            String plot = item.getMeta(MovieService.PLOT);
            if (plot != null)
                sb.append(plot.replace("\n", "</br>")).append("</br>");

            String taglines = item.getMeta(MovieService.TAGLINES);
            if (taglines != null)
                sb.append(taglines.replace("\n", "</br>")).append("</br>");
        }

        infoLabel.setValue(sb.toString());
        infoLabel.setContentMode(Label.CONTENT_XHTML);
    }

    public void refillComponents() {
        if (this.item == null) {
            return;
        }

        components.removeAllComponents();

        final Map<InformationItem, Double> uselessComponents = new HashMap<InformationItem, Double>();
        Map<InformationItem, Double> componentsMap = new HashMap<InformationItem, Double>();

        for (Map.Entry<InformationItem, Double> componentEntry : item.getComponents().entrySet()) {
            if ((componentEntry.getValue() > 0) && (componentEntry.getValue() < COMPONENT_THRESHOLD)) {
                uselessComponents.put(componentEntry.getKey(), componentEntry.getValue());
            } else {
                componentsMap.put(componentEntry.getKey(), componentEntry.getValue());
            }
        }


        addItemsToComponents(componentsMap);

        if (uselessComponents.size() > 0) {
            final Button loadButton = new Button(String.format("load other %d tags with low weight", uselessComponents.size()));
            loadButton.setWidth(null);
            loadButton.setStyleName(Stream.BUTTON_LINK);
            loadButton.addListener(new Button.ClickListener(){
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    components.removeComponent(loadButton);
                    addItemsToComponents(uselessComponents);
                }
            });
            components.addComponent(loadButton);
        }
    }

    private void addItemsToComponents(Map<InformationItem, Double> items) {
        service.reloadMetadata(items.keySet());
        for (Map.Entry<InformationItem, Double> componentEntry : sortByValue(items, false).entrySet()) {
            InformationItem item = componentEntry.getKey();
            ItemTag tag = new ItemTag(item, componentEntry.getValue(), ItemPage.class);
            tag.setWidth(null);

            if (item.getMeta("CREATED BY").equals(PersonService.class.getName())) {
                tag.addStyleName("ii-actor");
            }

            components.addComponent(tag);
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

        loader.addListener(Button.ClickEvent.class, this,"loadRecommendations");
    }

    public void loadRecommendations (Button.ClickEvent event) {
        stream.removeAllComponents();

        long startTime = System.currentTimeMillis();

        long limit = STREAM_SIZE_LIMIT;
        Map<InformationItem, Double> recommendations = recommender.getMostLike(item, service);

        for (InformationItem component : item.getComponents().keySet()) {
            recommendations.remove(component);
        }

        List<InformationItem> itemsToReload = new LinkedList<InformationItem>();
        List<ItemTag> tagsToAdd = new LinkedList<ItemTag>();

        for (Map.Entry<InformationItem, Double> entry : recommendations.entrySet()) {
            if (limit-- < 0)
                break;

            InformationItem recommendedItem = entry.getKey();
            Double value = entry.getValue();
            ItemTag tag = new ItemTag(recommendedItem, value, ItemTag.DEFAULT_COMPONENTS_LIMIT, ItemPage.class);
//            tag.setComponentsLimit(50);
            tag.setCommonItems(item.getComponents().keySet());
            tagsToAdd.add(tag);

            itemsToReload.add(recommendedItem);
            itemsToReload.addAll(tag.getDisplayedComponents());
        }

        service.reloadMetadata(itemsToReload);
        for(ItemTag tag : tagsToAdd)
            stream.addComponent(tag);

        getApplication().getMainWindow().showNotification(String.format("Recommendation search took %d seconds", (System.currentTimeMillis() - startTime) / 1000));
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
