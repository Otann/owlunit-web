package com.manymonkeys.app.auth;

import com.manymonkeys.app.MainApplication;
import com.manymonkeys.app.auth.window.LoginWindow;
import com.manymonkeys.app.page.ProfilePage;
import com.manymonkeys.core.ii.InformationItem;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class LoginButton extends Button implements MenuBar.Command, LoginWindow.UserLoggedInListener {

    private Window window;

    public LoginButton() {
        super("Sign In");

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
        window = new LoginWindow(this);
        MainApplication.getCurrentNavigableAppLevelWindow().addWindow(window);
    }

    private void closeWindow() {
        MainApplication.getCurrentNavigableAppLevelWindow().removeWindow(window);
    }

    @Override
    public void userLoggedIn(InformationItem user) {
        closeWindow();
        MainApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(ProfilePage.class);
    }
}
