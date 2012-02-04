package com.manymonkeys.web.crud.page;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.web.crud.control.*;
import org.apache.click.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrudPage extends Template {

    @SuppressWarnings("UnusedDeclaration")
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IiDao dao;

    public String title = "OWL CRUD";

    @Override
    public void onInit() {
        addControl(new LoadByUUIDForm(dao));
        addControl(new LoadByMetaForm(dao));
        addControl(new UpdateMetaForm(dao));
        addControl(new UpdateComponentForm(dao));
        addControl(new DeleteForm(dao));
    }

    @Override
    public void onRender() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public ActionResult onCreateIiClick() {
        try {
            Ii item = dao.createInformationItem();
            if (item == null) {
                return Utils.createMessageResult("Unexpected error occurred, unable to create item", Utils.Result.ERROR);
            } else {
                item = dao.loadMeta(item);
                item = dao.loadComponents(item);
                return Utils.createObjectResult(null, item, "Created Ii with uuid <strong>" + item.getId() + "</strong>", Utils.Result.SUCCESS);
            }
        } catch (Exception e) {
            return Utils.createMessageResult(e.getMessage(), Utils.Result.ERROR);
        }
    }

}