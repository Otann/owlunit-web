package com.manymonkeys.ui;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.ui.widgetset.client.ui.VItemTag;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import org.vaadin.navigator7.PageLink;
import org.vaadin.navigator7.ParamPageLink;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */

@ClientWidget(VItemTag.class)
public class ItemTag extends AbstractComponent {

    public static final int DEFAULT_COMPONENTS_LIMIT = 10;

    private InformationItem item;
    private Double value;
    private int componentsLimit = DEFAULT_COMPONENTS_LIMIT;

    private Class pageClass;

    public ItemTag(InformationItem item, Double value, Class pageClass) {
        this(item, value, 0, pageClass);
    }

    public ItemTag(InformationItem item, Double value, int componentsLimit, Class pageClass) {
        this.item = item;
        this.value = value;
        this.componentsLimit = componentsLimit;
        this.pageClass = pageClass;
        requestRepaintRequests();
    }

    public int getComponentsLimit() {
        return componentsLimit;
    }

    public void setComponentsLimit(int componentsLimit) {
        this.componentsLimit = componentsLimit;
        requestRepaint();
    }

    public InformationItem getItem() {
        return item;
    }

    public void setItem(InformationItem item) {
        this.item = item;
        requestRepaint();
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
        requestRepaint();
    }

    /**
     * Paints any needed component-specific things to the given UIDL stream. The
     * more general {@link #paint(com.vaadin.terminal.PaintTarget)} method handles all general
     * attributes common to all components, and it calls this method to paint
     * any component-specific attributes to the UIDL stream.
     *
     * @param target the target UIDL stream where the component should paint itself to
     * @throws com.vaadin.terminal.PaintException
     *          if the paint operation failed.
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (!"".equals(getStyleName())) {
            target.addAttribute("style", getStyleName());
        }

        PageLink link = new ParamPageLink(item.getMeta(TagService.NAME), pageClass, item.getUUID().toString());
        link.paint(target);

        Label valueLabel = new Label(String.format("%.0f", value == null ? 0D : value));
        valueLabel.setSizeUndefined();
        valueLabel.paint(target);

        if (componentsLimit > 0) {
            int limit = componentsLimit;
            for (InformationItem component : sortByValue(item.getComponents(), false).keySet()) {
                if (limit-- <= 0)
                    break;

                Link cmp = new ParamPageLink(component.getMeta(TagService.NAME), pageClass, component.getUUID().toString());
                cmp.paint(target);
            }
        }
    }

    @Override
    public String toString() {
        String name = item.getMeta(TagService.NAME);
        String out = name != null ? name : "InformationItem#%d" + item.getUUID().toString();
        if (value == null) {
            return out;
        } else {
            return String.format("%s [%.0f]", out, value);
        }
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
