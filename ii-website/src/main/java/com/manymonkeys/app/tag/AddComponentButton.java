package com.manymonkeys.app.tag;

import com.manymonkeys.app.tag.common.FilterTagButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.core.ii.InformationItem;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class AddComponentButton extends FilterTagButton {

    private ItemPage page;

    public AddComponentButton(ItemPage page) {
        super("Add Component", "Add Custom Component", "Add", page.getService());
        this.page = page;
    }

    @Override
    public void processItem(InformationItem item) {
        InformationItem pageItem = page.getItem();
        if (pageItem != null) {
            double initialWeight = 1;
            page.getService().setComponentWeight(pageItem, item, initialWeight);
            page.refillComponents();
            page.refillStream();
        }
    }
}
