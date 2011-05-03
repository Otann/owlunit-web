package com.manymonkeys.app;

import com.manymonkeys.app.auth.AuthManager;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Layout;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.browsercookies.BrowserCookies;
import org.vaadin.navigator7.window.NavigableAppLevelWindow;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Configurable(preConstruction = true)
public class MainAppLevelWindow extends NavigableAppLevelWindow {

    private AuthManager authManager;
    private MainMenu menu = new MainMenu();

    @Override
    protected Layout createMainLayout() {
        return new VerDashLayout();
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    @Override
    protected ComponentContainer createComponents() {

        menu = new MainMenu();
        this.addComponent(menu);

        BrowserCookies cookies = new BrowserCookies();
        this.addComponent(cookies);

        authManager = new AuthManager(cookies);

        Layout pageLayout = new VerDashLayout();
//        pageLayout.setSizeFull();
        this.addComponent(pageLayout);


        return pageLayout;
    }

    @Override
    public void changePage(Component pageParam) {
        super.changePage(pageParam);
        menu.pageChanged(pageParam);
    }
}
