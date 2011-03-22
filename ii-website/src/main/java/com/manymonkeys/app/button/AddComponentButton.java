package com.manymonkeys.app.button;

import com.manymonkeys.app.button.common.FilterTagButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.core.ii.InformationItem;
import com.vaadin.ui.Window;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class AddComponentButton extends FilterTagButton {

    private ItemPage page;

    public AddComponentButton(Window topWindow, ItemPage page) {
        super(topWindow, "Add Component", "Add Custom Component", "Add", page.getService());
        this.page = page;
    }

    @Override
    public void processItem(InformationItem item) {
        InformationItem pageItem = page.getItem();
        if (pageItem != null) {
            double initialWeight = page.getRecommender().calculateInitialWeight(pageItem, item);
            page.getService().setComponentWeight(pageItem, item, initialWeight);
            page.refillComponents();
            page.refillStream();
        }
    }
}
