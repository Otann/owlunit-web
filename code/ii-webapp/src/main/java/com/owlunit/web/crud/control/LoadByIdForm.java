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

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class LoadByIdForm extends ItemForm {

    private TextField idField;

    public LoadByIdForm(IiDao dao) {
        super("loadByIdForm", dao);
    }

    @Override
    public void onInit() {
        super.onInit();

        this.add(new Label("Load by Id"));

        idField = new TextField("id");
        idField.addStyleClass("span7");
        idField.setRequired(true);
        this.add(idField);

        Submit submit = new Submit("load", "Load");
        submit.addBehavior(new DefaultAjaxBehavior() {
            @Override
            public ActionResult onAction(Control source) {
                return onLoadById();
            }
        });
        this.add(submit);

        this.addBehavior(new DefaultAjaxBehavior());
    }

    private ActionResult onLoadById() {
        if (!this.isValid()) {
            return emptyResult();
        }

        try {
            long id = Long.parseLong(idField.getValue());
            Ii item = getDao().load(id);

            if (item != null) {
                return itemResult(reloadItem(item));
            } else {
                idField.setError("Unable to find item with this id");
                return emptyResult();
            }

        } catch (IllegalArgumentException e) {
            idField.setError("this is not valid id");
            return emptyResult();
        } catch (HectorException e) {
            this.setError(e.getMessage());
            return emptyResult();
        }
    }

}
