package com.manymonkeys.web.application.page;

import com.manymonkeys.moviesstory.model.MoviesStoryUser;
import com.manymonkeys.web.application.control.OwlSearchPanel;
import org.apache.click.util.Bindable;
import org.springframework.stereotype.Component;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
@Component
public class ProfilePage extends BorderTemplate {

    public String title = "Profile Page";

    @Bindable
    private OwlSearchPanel owlSearchPanel = new OwlSearchPanel();

    public ProfilePage() {
        super();
    }

    @Override
    public void onRender() {
        addModel("user", getContext().getSessionAttribute(MoviesStoryUser.class.getCanonicalName()));
    }
}