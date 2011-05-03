package com.manymonkeys.app.auth;

import com.manymonkeys.app.MainApplication;
import com.vaadin.ui.Button;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class LogoutButton extends Button implements MenuBar.Command {

    public LogoutButton() {
        super("Sign Out");

        addListener(new ClickListener() {
            public void buttonClick(ClickEvent clickEvent) {
                doLogout();
            }
        });
    }

    @Override
    public void menuSelected(MenuBar.MenuItem selectedItem) {
        doLogout();
    }

    private void doLogout() {
        Window mainWindow = MainApplication.getCurrentNavigableAppLevelWindow();
        AuthManager.getCurrent(mainWindow).deathenticate();
        MainApplication.navigateHome();
    }

}
