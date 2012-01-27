package com.manymonkeys.web.page.crud;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import me.prettyprint.hector.api.exceptions.HectorException;
import org.apache.click.ActionResult;
import org.apache.click.Control;
import org.apache.click.ajax.DefaultAjaxBehavior;
import org.apache.click.control.Label;
import org.apache.click.control.Submit;
import org.apache.click.control.TextField;

import java.util.UUID;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class LoadByUUIDForm extends ItemForm {

    private TextField uuidField;

    public LoadByUUIDForm(IiDao dao) {
        super("loadByUUIDForm", dao);
    }

    @Override
    public void onInit() {
        super.onInit();

        this.add(new Label("Load by UUID"));

        uuidField = new TextField("uuid");
        uuidField.addStyleClass("span7");
        uuidField.setRequired(true);
        this.add(uuidField);

        Submit submit = new Submit("load", "Load");
        submit.addBehavior(new DefaultAjaxBehavior() {
            @Override
            public ActionResult onAction(Control source) {
                return onLoadByUUID();
            }
        });
        this.add(submit);

        this.addBehavior(new DefaultAjaxBehavior());
    }

    private ActionResult onLoadByUUID() {
        if (!this.isValid()) {
            return emptyResult();
        }

        try {
            long id = Long.parseLong(uuidField.getValue());
            Ii item = getDao().load(id);

            if (item != null) {
                return itemResult(reloadItem(item));
            } else {
                uuidField.setError("Unable to find item with this uuid");
                return emptyResult();
            }

        } catch (IllegalArgumentException e) {
            uuidField.setError("this is not valid UUID");
            return emptyResult();
        } catch (HectorException e) {
            this.setError(e.getMessage());
            return emptyResult();
        }
    }

}
