package com.owlunit.web.crud.control;

import com.owlunit.core.ii.Ii;
import com.owlunit.core.ii.IiDao;
import org.apache.click.ActionResult;
import org.apache.click.Control;
import org.apache.click.ajax.DefaultAjaxBehavior;
import org.apache.click.control.Submit;
import org.apache.click.control.TextField;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class DeleteForm extends ItemForm {

    private TextField uuidField;

    public DeleteForm(IiDao dao) {
        super("deleteForm", dao);
    }

    @Override
    public void onInit() {
        super.onInit();

        uuidField = new TextField("uuid");
        uuidField.addStyleClass("span7");
        uuidField.setRequired(true);
        this.add(uuidField);

        Submit submit = new Submit("delete", "Delete");
        submit.addBehavior(new DefaultAjaxBehavior() {
            @Override
            public ActionResult onAction(Control source) {
                return onDelete();
            }
        });
        this.add(submit);

        this.addBehavior(new DefaultAjaxBehavior());
    }

    private ActionResult onDelete() {
        if (!this.isValid()) {
            return emptyResult();
        }
        
        try {

            long id = Long.parseLong(uuidField.getValue());
            Ii item = getDao().load(id);
            if (item == null) {
                uuidField.setError("Unable to find item with this uuid");
                return emptyResult();
            }
            
            getDao().deleteInformationItem(item);

            return Utils.createMessageResult("Item with uuid <strong>" + item.getId() + "</strong> deleted", Utils.Result.WARN);

        } catch (IllegalArgumentException e) {
            uuidField.setError("this is not valid UUID");
            return emptyResult();
        } catch (Exception e) {
            this.setError(e.getMessage());
            return emptyResult();
        }
    }

}
