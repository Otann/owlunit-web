package com.manymonkeys.app.tag;

import com.manymonkeys.app.tag.common.OpenDialogButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.core.ii.Ii;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class DeleteTagButton extends OpenDialogButton {

    private ItemPage page;

    public DeleteTagButton(ItemPage page) {
        super("Delete Tag", "Confirmation", "Delete");
        this.page = page;
    }

    @Override
    public Layout getDialogContent() {
        CssLayout layout = new CssLayout();
        layout.addComponent(new Label("Are you sure you want delete this tag?"));
        return layout;
    }

    @Override
    public void onSubmit() {
        Ii item = page.getItem();
        if (item != null) {
            page.setItemId(null);
            page.getService().deleteInformationItem(item);
        }
    }

}
