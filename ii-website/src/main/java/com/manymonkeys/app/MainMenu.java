package com.manymonkeys.app;

import com.manymonkeys.app.auth.AuthManager;
import com.manymonkeys.app.auth.CreateUserButton;
import com.manymonkeys.app.auth.LoginButton;
import com.manymonkeys.app.auth.LogoutButton;
import com.manymonkeys.app.tag.AddComponentButton;
import com.manymonkeys.app.tag.AddTagButton;
import com.manymonkeys.app.tag.DeleteTagButton;
import com.manymonkeys.app.tag.LoadTagButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.app.page.MonitoringPage;
import com.manymonkeys.app.page.ProfilePage;
import com.manymonkeys.app.page.SearchPage;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.auth.UserService;
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Configurable(preConstruction = true)
public class MainMenu extends MenuBar {

    @Autowired
    private UserService service;

    private MenuItem crud;

    private MenuItem user;
    private MenuItem addItemToUser;

    public MainMenu() {

        this.setWidth("100%");

        this.addItem("Add New Tag", new AddTagButton(service));
        this.addItem("Load Tag", new LoadTagButton(service));

        crud = this.addItem("Current Tag...", null);
        crud.setVisible(false);

        this.addItem("Search", new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                MainApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(SearchPage.class);
            }
        });

        this.addItem("Monitoring", new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                MainApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(MonitoringPage.class);
            }
        });

        user = this.addItem("User...", null);
        reloadUserMenu(null);
    }

    public void pageChanged(final Component pageParam) {
        crud.setVisible(false);
        if (pageParam instanceof ItemPage) {
            final ItemPage page = (ItemPage) pageParam;
            crud.setVisible(true);
            crud.removeChildren();
            crud.addItem("Add Component", new AddComponentButton(page));
            crud.addItem("Delete Tag", new DeleteTagButton(page));

            reloadUserMenu(page);
        }
    }

    void reloadUserMenu(final ItemPage page) {
        user.removeChildren();

        user.addItem("Sign Up", new CreateUserButton());
        user.addItem("Sign In", new LoginButton());
        user.addItem("Sigh Out", new LogoutButton());
        user.addItem("Profile", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                MainApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(ProfilePage.class);
            }
        });

        if (page != null) {
            user.addItem("Add current tag to profile", new MenuBar.Command() {
                @Override
                public void menuSelected(MenuBar.MenuItem selectedItem) {
                    Window mainWindow = getApplication().getMainWindow();
                    InformationItem user;
                    try {
                        user = AuthManager.getCurrent(mainWindow).getCurrentUser();
                    } catch (AuthManager.AuthException e) {
                        mainWindow.showNotification("No user is logged in.", Window.Notification.TYPE_ERROR_MESSAGE);
                        return;
                    }

                    InformationItem item = page.getItem();
                    if (item == null) {
                        mainWindow.showNotification("No item selected.", Window.Notification.TYPE_ERROR_MESSAGE);
                        return;
                    }

                    service.setComponentWeight(user, item, 10d);
                    getApplication().getMainWindow().showNotification("Done");
                }
            });
        }
    }
}
