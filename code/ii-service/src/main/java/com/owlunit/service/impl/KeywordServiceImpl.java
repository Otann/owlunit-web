package com.owlunit.service.impl;

import com.owlunit.core.orthodoxal.ii.Ii;
import com.owlunit.core.orthodoxal.ii.IiDao;
import com.owlunit.model.cinema.Keyword;
import com.owlunit.service.cinema.KeywordService;
import com.owlunit.service.exception.NotFoundException;
import com.owlunit.service.impl.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class KeywordServiceImpl implements KeywordService {

    protected IiDao dao;

    private static final String CLASS_MARK_KEY = KeywordServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";

    static Keyword iiToKeyword(IiDao dao, Ii item) {
        Ii meta = Utils.itemWithMeta(dao, item);
        if (!meta.getMeta(CLASS_MARK_KEY).equals(CLASS_MARK_VALUE)) {
            throw new IllegalArgumentException("This is not a keyword");
        }
        return new Keyword(
                item.getId(),
                meta.getMeta(META_KEY_NAME)
        );
    }

    static Ii keywordToIi(IiDao dao, Keyword keyword) throws NotFoundException {
        Ii item = dao.load(keyword.getId());
        if (item == null) {
            throw new NotFoundException(String.format("Keyword(%s)", keyword.getName()));
        } else {
            return item;
        }
    }

    @Override
    public Keyword createKeyword(String name) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("Can not create keyword with empty or null name");
        }

        Ii item = dao.createInformationItem();
        dao.setMetaUnindexed(item, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        dao.setMeta(item, META_KEY_NAME, name);
        return iiToKeyword(dao, item);
    }

    @Override
    public Keyword loadById(Long id) throws NotFoundException {
        Ii item = dao.load(id);
        if (item == null) {
            throw new NotFoundException(String.format("Keyword(%d)", id));
        } else {
            return iiToKeyword(dao, item);
        }
    }

    @Override
    public Keyword loadByName(String name) throws NotFoundException {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("Can not load keyword with empty or null name");
        }

        Collection<Ii> blankItems = dao.load(META_KEY_NAME, name);
        if (blankItems.isEmpty()) {
            throw new NotFoundException(String.format("Keyword(%s)", name));
        } else {
            Ii meta = dao.loadMeta(blankItems.iterator().next());
            return iiToKeyword(dao, meta);
        }
    }

    @Override
    public Keyword loadOrCreateKeyword(String name) {
        try {
            return loadByName(name);
        } catch (NotFoundException e) {
            return createKeyword(name);
        }
    }

    @Override
    public List<Keyword> listKeywords() {
        Collection<Ii> blankItems = dao.load(CLASS_MARK_KEY, CLASS_MARK_VALUE);
        return iisToKeywords(dao.loadMeta(blankItems));
    }

    @Override
    public Keyword update(Keyword keyword) throws NotFoundException {
        Ii item = keywordToIi(dao, keyword);
        Ii updated = dao.setMeta(item, META_KEY_NAME, keyword.getName());
        return iiToKeyword(dao, updated);
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \_________________ */

    private List<Keyword> iisToKeywords(Collection<Ii> keywordsIi) {
        List<Keyword> result = new ArrayList<Keyword>();
        for (Ii ii : keywordsIi) {
            result.add(iiToKeyword(dao, ii));
        }
        return result;
    }

    /*-- - - - - - - - - - - - - - - - -\
    |   G E T T E R S  &  S E T T E R S |
    \__________________________________*/

    public IiDao getDao() {
        return dao;
    }

    public void setDao(IiDao dao) {
        this.dao = dao;
    }
}
