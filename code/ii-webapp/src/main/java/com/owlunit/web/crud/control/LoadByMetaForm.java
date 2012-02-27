package com.owlunit.web.crud.control;

import com.owlunit.core.ii.Ii;
import com.owlunit.core.ii.IiDao;
import me.prettyprint.hector.api.exceptions.HectorException;
import org.apache.click.ActionResult;
import org.apache.click.Control;
import org.apache.click.ajax.DefaultAjaxBehavior;
import org.apache.click.control.Label;
import org.apache.click.control.Submit;
import org.apache.click.control.TextField;

import java.util.Collection;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class LoadByMetaForm extends ItemForm {

    private TextField keyField;
    private TextField valueField;

    public LoadByMetaForm(IiDao dao) {
        super("loadByMetaForm", dao);
    }

    @Override
    public void onInit() {
        super.onInit();

        this.add(new Label("Load by Meta"));

        keyField = new TextField("key");
        keyField.setRequired(true);
        keyField.addStyleClass("span7");
        this.add(keyField);

        valueField = new TextField("value");
        valueField.addStyleClass("span7");
        this.add(valueField);

        Submit submit = new Submit("load", "Load");
        submit.addBehavior(new DefaultAjaxBehavior() {
            @Override
            public ActionResult onAction(Control source) {
                return onLoadByMeta();
            }
        });
        this.add(submit);

        this.addBehavior(new DefaultAjaxBehavior());
    }

    private ActionResult onLoadByMeta() {
        if (!this.isValid()) {
            return emptyResult();
        }

        try {
            String key = keyField.getValue();
            String value = valueField.getValue();
            Collection<Ii> rawItems = getDao().load(key, value);
            Collection<Ii> itemsWithMeta = getDao().loadMeta(rawItems);

            return itemsResult(itemsWithMeta);

        } catch (HectorException e) {
            this.setError(e.getMessage());
            return emptyResult();
        }
    }
}
