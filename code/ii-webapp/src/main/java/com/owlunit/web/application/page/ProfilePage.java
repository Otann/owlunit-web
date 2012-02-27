package com.owlunit.web.application.page;

import com.owlunit.moviesstory.model.MoviesStoryUser;
import com.owlunit.web.application.control.OwlSearchPanel;
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