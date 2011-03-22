package com.manymonkeys.app.button.common;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public abstract class OpenDialogButton extends Button implements MenuBar.Command {

    protected Window topWindow;
    protected Window subWindow;

    private String subWindowName;
    private String applyButtonTitle;

    protected Button submitButton;

    protected OpenDialogButton(final Window topWindow, String buttonName, String subWindowName, String applyButtonTitle) {
        super(buttonName);

        this.topWindow = topWindow;
        this.subWindowName = subWindowName;
        this.applyButtonTitle = applyButtonTitle;

        addListener(new ClickListener() {
            public void buttonClick(ClickEvent clickEvent) {
                openSubWindow();
            }
        });
    }

    /**
     * Called while creating dialog window to get window content
     *
     * @return component to add
     */
    public abstract Layout getDialogContent();

    /**
     * Called when apply button clicked
     */
    public abstract void onSubmit();

    public void menuSelected(MenuBar.MenuItem selectedItem) {
        openSubWindow();
    }

    private void openSubWindow() {
        subWindow = new Window(subWindowName);
        subWindow.setResizable(false);
        subWindow.setModal(true);
        VerticalLayout layout = new VerticalLayout();
        subWindow.setContent(layout);
        layout.setMargin(true);

        Layout subLayout = getDialogContent();
        layout.addComponent(subLayout);
        subLayout.setSizeFull();
        subLayout.setMargin(true);
        layout.setExpandRatio(subLayout, 1);

        submitButton = new Button(applyButtonTitle);
        submitButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        layout.addComponent(submitButton);
        layout.setComponentAlignment(submitButton, Alignment.MIDDLE_CENTER);

        submitButton.addListener(new ClickListener() {
            public void buttonClick(ClickEvent clickEvent) {
                onSubmit();
                topWindow.removeWindow(subWindow);
            }
        });

        topWindow.addWindow(subWindow);
    }

}
