package com.manymonkeys.web.page.crud;

import com.manymonkeys.controls.BootstrapForm;
import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import org.apache.click.ActionResult;

import java.util.Collection;

import static com.manymonkeys.web.page.crud.Utils.createItemResult;
import static com.manymonkeys.web.page.crud.Utils.createItemsResult;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public abstract class ItemForm extends BootstrapForm {
    
    private IiDao dao;

    public ItemForm(String name, IiDao dao) {
        super(name);
        this.dao = dao;
    }

    public IiDao getDao() {
        return dao;
    }
    
    protected ActionResult emptyResult() {
        return createItemsResult(this.toString(), null);
    }

    protected ActionResult itemResult(Ii item) {
        return createItemResult(this.toString(), item);
    }

    protected ActionResult itemsResult(Collection<Ii> items) {
        return createItemsResult(this.toString(), items);
    }
    
    protected Ii    reloadItem(Ii item) {
        item = getDao().loadMeta(item);
        return getDao().loadComponents(item);
    }

}
