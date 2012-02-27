package com.manymonkeys.web.crud.control;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
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

public class SearchByMetaForm extends ItemForm {

    private TextField keyField;
    private TextField prefixField;

    public SearchByMetaForm(IiDao dao) {
        super("searchByMetaForm", dao);
    }

    @Override
    public void onInit() {
        super.onInit();

        this.add(new Label("Search by Meta"));

        keyField = new TextField("key");
        keyField.setRequired(true);
        keyField.addStyleClass("span7");
        this.add(keyField);

        prefixField = new TextField("prefix");
        prefixField.addStyleClass("span7");
        this.add(prefixField);

        Submit submit = new Submit("load", "Load");
        submit.addBehavior(new DefaultAjaxBehavior() {
            @Override
            public ActionResult onAction(Control source) {
                return onSearchByMeta();
            }
        });
        this.add(submit);

        this.addBehavior(new DefaultAjaxBehavior());
    }

    private ActionResult onSearchByMeta() {
        if (!this.isValid()) {
            return emptyResult();
        }

        try {
            String key = keyField.getValue();
            String prefix = prefixField.getValue();
            Collection<Ii> rawItems = getDao().search(key, prefix);
            Collection<Ii> itemsWithMeta = getDao().loadMeta(rawItems);

            return itemsResult(itemsWithMeta);

        } catch (HectorException e) {
            this.setError(e.getMessage());
            return emptyResult();
        }
    }
}
