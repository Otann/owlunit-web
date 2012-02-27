package com.owlunit.web.application.page;

import com.owlunit.web.application.control.OwlSearchPanel;
import org.apache.click.util.Bindable;
import org.springframework.stereotype.Component;

@Component
public class InspirationPage extends BorderTemplate {

    public String title = "Inspiration Page";

    @Bindable
    private OwlSearchPanel owlSearchPanel = new OwlSearchPanel();

    public InspirationPage() {
        super();
    }

}