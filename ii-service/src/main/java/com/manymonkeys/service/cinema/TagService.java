package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import org.springframework.beans.factory.annotation.Autowired;
import static com.manymonkeys.service.cinema.Utils.itemWithMeta;

import java.util.Collection;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class TagService {

    @Autowired
    protected IiDao dao;

    private static final String CLASS_MARK_KEY = TagService.class.getName();
    private static final String CLASS_MARK_VALUE = "#";
    private static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";

    public Ii createTag(String name) {
        Ii item = dao.createInformationItem();
        dao.setUnindexedMeta(item, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(item, META_KEY_NAME, name);
        return item;
    }

    public Ii getTag(String name) {
        //TODO Anton Chebotaev - discuss if we have unique tags or not
        Collection<Ii> blankItems = dao.load(META_KEY_NAME, name);
        if (blankItems.isEmpty()) {
            return null;
        } else {
            return dao.loadMetadata(blankItems).iterator().next();
        }
    }

    public Collection<Ii> getAll() {
        Collection<Ii> blankItems = dao.load(CLASS_MARK_KEY, CLASS_MARK_VALUE);
        return dao.loadMetadata(blankItems);
    }

    public String getName(Ii item) {
        return itemWithMeta(dao, item).getMeta(META_KEY_NAME);
    }

    public Ii setName(Ii person, String name) {
        return dao.setMeta(person, META_KEY_NAME, name);
    }

    public boolean isTag(Ii item) {
        return itemWithMeta(dao, item).getMeta(CLASS_MARK_KEY) != null;
    }

}
