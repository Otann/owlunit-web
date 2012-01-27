package com.manymonkeys.web.page.crud;

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

import java.util.UUID;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class UpdateComponentForm extends ItemForm {

    private TextField itemField;
    private TextField componentField;
    private TextField valueField;

    public UpdateComponentForm(IiDao dao) {
        super("updateComponentForm", dao);
    }

    @Override
    public void onInit() {
        super.onInit();

        this.add(new Label("Update item's components"));

        itemField = new TextField("item", "Item's uuid");
        itemField.addStyleClass("span7");
        itemField.setRequired(true);
        this.add(itemField);

        componentField = new TextField("component", "Component's uuid");
        componentField.setRequired(true);
        componentField.addStyleClass("span7");
        this.add(componentField);

        valueField = new TextField("value", "Connection value");
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

            Ii item = null;
            Ii component = null;

            try {
                long id = Long.parseLong(itemField.getValue());
                item = getDao().load(id);
                if (item == null) {
                    itemField.setError("can not find item with this uuid");
                }
            }  catch (IllegalArgumentException e) {
                itemField.setError("this is not valid UUID");
            }
            try {
                long id = Long.parseLong(componentField.getValue());
                component = getDao().load(id);
                if (component == null) {
                    itemField.setError("can not find item with this uuid");
                }
            }  catch (IllegalArgumentException e) {
                componentField.setError("this is not valid UUID");
            }
            
            if (item == null || component == null) {
                return emptyResult();
            }
            
            Double value = null;
            try {
                value = Double.parseDouble(valueField.getValue());
            } catch (NumberFormatException e) {
                valueField.setError("this is not correct value");
            }

            if (value == null) {
                getDao().removeComponent(item, component);
            } else {
                getDao().setComponentWeight(item, component, value);
            }
            
            return itemResult(reloadItem(item));

        } catch (HectorException e) {
            this.setError(e.getMessage());
            return emptyResult();
        }
    }

}
