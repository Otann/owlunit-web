package com.manymonkeys.app.auth;

import com.manymonkeys.app.MainApplication;
import com.manymonkeys.app.auth.window.CreateUserWindow;
import com.manymonkeys.core.ii.InformationItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CreateUserButton extends Button implements MenuBar.Command, CreateUserWindow.UserCreatedListener {

    private Window window;

    public CreateUserButton() {
        super("Sign Up");

        addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                openWindow();
            }
        });
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        openWindow();
    }

    private void openWindow() {
        window = new CreateUserWindow(this);
        MainApplication.getCurrentNavigableAppLevelWindow().addWindow(window);
    }

    private void closeWindow() {
        MainApplication.getCurrentNavigableAppLevelWindow().removeWindow(window);
    }

    @Override
    public void userCreated(InformationItem user) {
        closeWindow();
    }
}
