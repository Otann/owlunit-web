package com.manymonkeys.app;

import com.manymonkeys.app.page.*;
import org.vaadin.navigator7.WebApplication;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class MainWebApplication extends WebApplication {

    public static Class homePage() {
        return SearchPage.class;
    }

    public MainWebApplication() {
//        registerPages("com.manymonkeys.app.page");
        registerPages(new Class[]{
                PerformanceTestPage.class,
                SearchPage.class,
                ItemPage.class,
                MonitoringPage.class,
                ProfilePage.class
        });
    }


}
