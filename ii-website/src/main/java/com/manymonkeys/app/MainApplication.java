package com.manymonkeys.app;

import com.manymonkeys.app.page.SearchPage;
import org.springframework.beans.factory.annotation.Configurable;
import org.vaadin.navigator7.NavigableApplication;
import org.vaadin.navigator7.window.NavigableAppLevelWindow;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
@Configurable(preConstruction = true)
public class MainApplication extends NavigableApplication {

    @Override
    public NavigableAppLevelWindow createNewNavigableAppLevelWindow() {
        return new MainAppLevelWindow();
    }

    @Override
    public void init() {
        setTheme("stream");
        setMainWindow(createNewNavigableAppLevelWindow());
    }

    public static void navigateHome() {
        getCurrentNavigableAppLevelWindow().getNavigator().navigateTo(SearchPage.class);
    }


}
