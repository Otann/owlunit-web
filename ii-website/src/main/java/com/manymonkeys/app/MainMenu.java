package com.manymonkeys.app;

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
import com.manymonkeys.service.cinema.TagService;
import com.vaadin.ui.Component;
import com.vaadin.ui.MenuBar;
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
    private TagService service;

    private MenuBar.MenuItem crud;

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

        MenuBar.MenuItem users = this.addItem("User...", null);
        users.addItem("Sign Up", new CreateUserButton());
        users.addItem("Sign In", new LoginButton());
        users.addItem("Sigh Out", new LogoutButton());
        users.addItem("Profile", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                MainApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(ProfilePage.class);
            }
        });

    }

    public void pageChanged(final Component pageParam) {
        crud.setVisible(false);
        if (pageParam instanceof ItemPage) {
            ItemPage page = (ItemPage) pageParam;
            crud.setVisible(true);
            crud.removeChildren();
            crud.addItem("Add Component", new AddComponentButton(page));
            crud.addItem("Delete Tag", new DeleteTagButton(page));
        }

    }
}
