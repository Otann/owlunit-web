package com.manymonkeys.app;

import com.manymonkeys.app.button.AddComponentButton;
import com.manymonkeys.app.button.AddTagButton;
import com.manymonkeys.app.button.DeleteTagButton;
import com.manymonkeys.app.button.LoadTagButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.app.page.MonitoringPage;
import com.manymonkeys.app.page.SearchPage;
import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.service.cinema.TagService;
import com.manymonkeys.spring.SpringContextHelper;
import com.vaadin.incubator.dashlayout.ui.VerDashLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.window.NavigableAppLevelWindow;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
@Configurable(preConstruction = true)
public class MainAppLevelWindow extends NavigableAppLevelWindow {

    @Autowired
    private TagService service;

    @Autowired
    private Recommender recommender;

    MenuBar menu;
    MenuBar.MenuItem crud;

    @Override
    protected Layout createMainLayout() {
        return new VerDashLayout();
    }

    @Override
    protected ComponentContainer createComponents() {
        //TODO: fix this after @Autowired gets fixed
//        SpringContextHelper helper = new SpringContextHelper(getApplication());
//        service = (TagService) helper.getBean("iiService");
//        recommender = (Recommender) helper.getBean("iiRecommender");

        menu = new MenuBar();
        this.addComponent(menu);
        menu.setWidth("100%");

        menu.addItem("Add New Tag", new AddTagButton(this, service));
        menu.addItem("Load Tag", new LoadTagButton(this, service));

        crud = menu.addItem("Current Tag...", null);
        crud.setVisible(false);

        menu.addItem("Search", new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                MainApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(SearchPage.class);
            }
        });

        menu.addItem("Monitoring", new MenuBar.Command() {
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                MainApplication.getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(MonitoringPage.class);
            }
        });

        Layout pageLayout = new VerDashLayout();
//        pageLayout.setHeight(Sizeable.SIZE_UNDEFINED, Sizeable.UNITS_PIXELS);
        this.addComponent(pageLayout);

        return pageLayout;
    }

    @Override
    public void changePage(Component pageParam) {
        super.changePage(pageParam);

        crud.setVisible(false);
        if (pageParam instanceof ItemPage) {
            ItemPage page = (ItemPage) pageParam;
            crud.setVisible(true);
            crud.removeChildren();
            crud.addItem("Add Component", new AddComponentButton(getApplication().getMainWindow(), page));
            crud.addItem("Delete Tag", new DeleteTagButton(getApplication().getMainWindow(), page));
        }
    }
}
