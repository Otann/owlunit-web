package com.manymonkeys.web.application.page;

import com.manymonkeys.web.application.control.OwlSearchPanel;
import org.apache.click.util.Bindable;
import org.springframework.stereotype.Component;

@Component
public class PrevSearchesPage extends BorderTemplate {

    public String title = "Prev. Searchers Page";

    @Bindable
    private OwlSearchPanel owlSearchPanel = new OwlSearchPanel();

    public PrevSearchesPage() {
        super();
    }

}