package com.manymonkeys.app.button;


import com.manymonkeys.app.button.common.OpenDialogButton;
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class AddTagButton extends OpenDialogButton {

    private TagService service;
    private TextField tagName;

    public AddTagButton(Window topWindow, TagService service) {
        super(topWindow, "Create Tag", "Create Custom Tag With Name", "Create");
        this.service = service;
        tagName = new TextField();
    }

    @Override
    public Layout getDialogContent() {
        CssLayout rootLayout = new CssLayout();
        rootLayout.addComponent(tagName);
        tagName.setWidth("100%");
        return rootLayout;
    }

    @Override
    public void onSubmit() {
        String name = (String) tagName.getValue();
        if (name != null && service.getTag(name) == null) {
            service.createTag(name);
            topWindow.removeWindow(subWindow);
        } else {
            topWindow.showNotification("Can't add empty or existing tag", Window.Notification.TYPE_WARNING_MESSAGE);
        }
    }

}
