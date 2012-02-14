package com.manymonkeys.web.application.page;

import com.manymonkeys.web.application.control.OwlSearchPanel;
import org.apache.click.util.Bindable;
import org.springframework.stereotype.Component;

@Component
public class CommunityPage extends BorderTemplate {

    public String title = "Community Page";

    @Bindable
    private OwlSearchPanel owlSearchPanel = new OwlSearchPanel();

    public CommunityPage() {
        super();
    }

}