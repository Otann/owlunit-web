package com.manymonkeys.ui.widgetset.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.VLabel;
import com.vaadin.terminal.gwt.client.ui.VLink;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class VItemTag extends Composite implements Paintable {

    public static final String CLASSNAME_TAG = "v-iitag";
    public static final String CLASSNAME_CAPTION = "v-iitag-name";
    public static final String CLASSNAME_VALUE = "v-iitag-value";
    public static final String CLASSNAME_COMPONENTS = "v-iitag-components";
    public static final String CLASSNAME_COMPONENT = "v-iitag-component";
    public static final String CLASSNAME_SEPARATOR = "v-itemtag-separator";
    FlowPanel root;

    VLink name;
    VLabel value;
    List<VLink> components;

    public VItemTag() {
        root = new FlowPanel();
        root.setStylePrimaryName(CLASSNAME_TAG);
        initWidget(root);
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        final Iterator<Object> iterator = uidl.getChildIterator();

        if(uidl.hasAttribute("style")) {
            root.addStyleName(uidl.getStringAttribute("style"));
        }

        UIDL r = (UIDL) iterator.next();
        name = (VLink) client.getPaintable(r);
        name.updateFromUIDL(r, client);

        r = (UIDL) iterator.next();
        value = (VLabel) client.getPaintable(r);
        value.updateFromUIDL(r, client);

        components = new ArrayList<VLink>();
        while (iterator.hasNext()) {
            r = (UIDL) iterator.next();
            VLink component = (VLink) client.getPaintable(r);
            component.updateFromUIDL(r, client);
            components.add(component);
        }

        repaint();
    }

    private void repaint() {
        root.clear();

        name.setStylePrimaryName(CLASSNAME_CAPTION);
        root.add(name);

        value.setStylePrimaryName(CLASSNAME_VALUE);
        root.add(value);

        if (!components.isEmpty()) {
            VLabel separator = new VLabel();
            separator.setStylePrimaryName(CLASSNAME_SEPARATOR);
            root.add(separator);

            for (VLink component : components) {
                component.setStylePrimaryName(CLASSNAME_COMPONENT);
                root.add(component);
            }

            separator = new VLabel();
            separator.setStylePrimaryName(CLASSNAME_SEPARATOR);
            root.add(separator);
        }
    }
}
