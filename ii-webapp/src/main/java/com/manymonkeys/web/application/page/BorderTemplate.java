package com.manymonkeys.web.application.page;

import org.apache.click.Page;
import org.apache.click.extras.control.Menu;
import org.apache.click.extras.control.MenuFactory;
import org.apache.click.util.Bindable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class BorderTemplate extends Page {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Bindable
    protected Menu rootMenu = new MenuFactory().getRootMenu("rootMenu", "/application/menu.xml");

    public String getTemplate() {
        return "/application/page/border-template.htm";
    }
}
