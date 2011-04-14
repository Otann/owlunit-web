package com.manymonkeys.app.tag;


import com.manymonkeys.app.MainApplication;
import com.manymonkeys.app.tag.common.FilterTagButton;
import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.TagService;
import org.vaadin.navigator7.Navigator;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class LoadTagButton extends FilterTagButton {

    public LoadTagButton(TagService service) {
        super("Load Tag", "Select Tag To Load", "Load", service);
    }

    @Override
    public void processItem(InformationItem item) {
        Navigator nav = MainApplication.getCurrentNavigableAppLevelWindow().getNavigator();
        nav.navigateTo(ItemPage.class, item.getUUID().toString());
    }
}
