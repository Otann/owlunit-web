package com.manymonkeys.app.tag;

import com.manymonkeys.app.tag.common.FilterTagButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.core.ii.Ii;

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
    public void processItem(Ii item) {
        Ii pageItem = page.getItem();
        if (pageItem != null) {
            double initialWeight = 10;
            page.getService().setComponentWeight(pageItem, item, initialWeight);
            page.refillComponents();
            page.refillStream();
        }
    }
}
