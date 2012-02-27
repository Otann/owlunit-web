package com.owlunit.web.crud.control;

import com.owlunit.core.ii.Ii;
import com.owlunit.core.ii.IiDao;
import org.apache.click.ActionResult;

import java.util.Collection;

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
        return Utils.createItemsResult(this.toString(), null);
    }

    protected ActionResult itemResult(Ii item) {
        return Utils.createItemResult(this.toString(), item);
    }

    protected ActionResult itemsResult(Collection<Ii> items) {
        return Utils.createItemsResult(this.toString(), items);
    }
    
    protected Ii    reloadItem(Ii item) {
        item = getDao().loadMeta(item);
        return getDao().loadComponents(item);
    }

}
