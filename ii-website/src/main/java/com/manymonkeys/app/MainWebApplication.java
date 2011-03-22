package com.manymonkeys.app;

import com.manymonkeys.app.page.ItemPage;
import com.manymonkeys.app.page.MonitoringPage;
import com.manymonkeys.app.page.PerformanceTestPage;
import com.manymonkeys.app.page.SearchPage;
import org.vaadin.navigator7.WebApplication;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class MainWebApplication extends WebApplication {

    public MainWebApplication() {
//        registerPages("com.manymonkeys.app.page");
        registerPages(new Class[]{
                PerformanceTestPage.class,
                SearchPage.class,
                ItemPage.class,
                MonitoringPage.class
        });
    }


}
