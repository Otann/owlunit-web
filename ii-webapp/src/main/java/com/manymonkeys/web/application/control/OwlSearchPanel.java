package com.manymonkeys.web.application.control;

import org.apache.click.Context;
import org.apache.click.Stateful;
import org.apache.click.control.AbstractControl;
import org.apache.click.element.CssImport;
import org.apache.click.element.JsImport;
import org.apache.click.util.ClickUtils;
import org.apache.click.util.HtmlStringBuffer;

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
public class OwlSearchPanel extends AbstractControl implements Stateful {

    private static final String CONTROLLER_TAG = "div";
    public static final String CONTROLLER_NAME = "owlSearchPanel";

    /*--------------------\
    |  C O M P O N E N T  |
    \====================*/

    public OwlSearchPanel() {
        super(CONTROLLER_NAME);
    }

    @Override
    public void onInit() {
        super.onInit();

        getHeadElements().add(new JsImport("/application/control/owl-search-panel.js"));
        getHeadElements().add(new CssImport("/application/control/owl-search-panel.css"));
    }

    @Override
    public void render(HtmlStringBuffer buffer) {

        buffer.elementStart(getTag());
        buffer.appendAttribute("id", getId());
        buffer.elementEnd();

        buffer.append("<span class=\"owl\" style=\"color: blue;\">OwlSearchControl</span>");

        buffer.elementEnd(getTag());
    }

    @Override
    public String getTag() {
        return CONTROLLER_TAG;
    }

    /*-------------------------------------------\
    |  K E E P I N G  T R A C K  O F  S T A T E  |
    \===========================================*/

    public void removeState(Context context) {
        ClickUtils.removeState(this, getName(), context);
    }

    public void restoreState(Context context) {
        ClickUtils.restoreState(this, getName(), context);
    }

    public void saveState(Context context) {
        ClickUtils.saveState(this, getName(), context);
    }

    @Override
    public Object getState() {
        return null;
    }

    @Override
    public void setState(Object state) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
