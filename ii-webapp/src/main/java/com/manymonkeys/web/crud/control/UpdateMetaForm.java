package com.manymonkeys.web.crud.control;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import me.prettyprint.hector.api.exceptions.HectorException;
import org.apache.click.ActionResult;
import org.apache.click.Control;
import org.apache.click.ajax.DefaultAjaxBehavior;
import org.apache.click.control.Button;
import org.apache.click.control.Label;
import org.apache.click.control.Submit;
import org.apache.click.control.TextField;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class UpdateMetaForm extends ItemForm {

    private TextField idField;
    private TextField keyField;
    private TextField valueField;

    public UpdateMetaForm(IiDao dao) {
        super("updateMetaForm", dao);
    }

    @Override
    public void onInit() {
        super.onInit();

        this.add(new Label("Update item's metadata"));
        
        idField = new TextField("id");
        idField.addStyleClass("span7");
        idField.setRequired(true);
        this.add(idField);

        keyField = new TextField("key");
        keyField.setRequired(true);
        keyField.addStyleClass("span7");
        this.add(keyField);

        valueField = new TextField("value");
        valueField.addStyleClass("span7");
        this.add(valueField);

        Submit submit = new Submit("updateAndLoad", "Update and Load");
        submit.addBehavior(new DefaultAjaxBehavior() {
            @Override
            public ActionResult onAction(Control source) {
                return onUpdate();
            }
        });
        this.add(submit);

        Button update = new Button("update", "Update");
        update.addBehavior(new DefaultAjaxBehavior() {
            @Override
            public ActionResult onAction(Control source) {
                return onUpdate();
            }
        });
        this.add(update);

        this.addBehavior(new DefaultAjaxBehavior());
    }

    private ActionResult onUpdate() {
        if (!this.isValid()) {
            return emptyResult();
        }

        try {

            long id = Long.parseLong(idField.getValue());
            Ii item = getDao().load(id);

            if (item == null) {
                idField.setError("Unable to find item with this id");
                return emptyResult();
            } else {
                String key = keyField.getValue();
                String value = valueField.getValue();
                if (value != null) {
                    item = getDao().setMeta(item, key, value);
                } else {
                    item = getDao().removeMeta(item, key);
                }
                return itemResult(reloadItem(item));
            }

        } catch (IllegalArgumentException e) {
            idField.setError("this is not valid Id");
            return emptyResult();
        } catch (HectorException e) {
            this.setError(e.getMessage());
            return emptyResult();
        }
    }

}
