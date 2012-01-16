package com.manymonkeys.web.page;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.web.page.crud.*;
import org.apache.click.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static com.manymonkeys.web.page.crud.Utils.*;

@Component
public class CrudPage extends TemplatePage {

    @SuppressWarnings("UnusedDeclaration")
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("cassandraDao")
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
                return createMessageResult("Unexpected error occurred, unable to create item", Result.ERROR);
            } else {
                item = dao.loadMeta(item);
                item = dao.loadComponents(item);
                return createObjectResult(null, item, "Created Ii with uuid <strong>" + item.getUUID().toString() + "</strong>", Result.SUCCESS);
            }
        } catch (Exception e) {
            return createMessageResult(e.getMessage(), Result.ERROR);
        }
    }

}