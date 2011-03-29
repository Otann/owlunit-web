package com.manymonkeys.app.button;

import com.manymonkeys.app.button.common.OpenDialogButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.core.ii.InformationItem;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Window;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class DeleteTagButton extends OpenDialogButton {

    private ItemPage page;

    public DeleteTagButton(Window topWindow, ItemPage page) {
        super(topWindow, "Delete Tag", "Confirmation", "Delete");
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
        InformationItem item = page.getItem();
        if (item != null) {
            page.setItem(null);
            page.getService().deleteInformationItem(item);
        }
    }

}
