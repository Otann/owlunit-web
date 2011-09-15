package com.manymonkeys.service.cinema.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.cinema.Keyword;
import com.manymonkeys.service.cinema.KeywordService;
import com.manymonkeys.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.manymonkeys.service.cinema.util.Utils.itemWithMeta;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class KeywordServiceImpl implements KeywordService {

    @Autowired
    protected IiDao dao;

    private static final String CLASS_MARK_KEY = KeywordServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    private static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";

    @Override
    public Keyword createKeyword(String name) {
        Ii item = dao.createInformationItem();
        dao.setUnindexedMeta(item, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        dao.setMeta(item, META_KEY_NAME, name);
        return toKeyword(item);
    }

    @Override
    public Keyword loadKeyword(UUID uuid) {
        return toKeyword(dao.load(uuid));
    }

    @Override
    public Keyword loadKeyword(String name) {
        Collection<Ii> blankItems = dao.load(META_KEY_NAME, name);
        if (blankItems.isEmpty()) {
            return null;
        } else {
            return toKeyword(dao.loadMetadata(blankItems).iterator().next());
        }
    }

    @Override
    public List<Keyword> listKeywords() {
        Collection<Ii> blankItems = dao.load(CLASS_MARK_KEY, CLASS_MARK_VALUE);
        return toKeyword(dao.loadMetadata(blankItems));
    }

    @Override
    public Keyword update(Keyword keyword) throws NotFoundException {
        assert keyword.getUuid() != null;
        return toKeyword(dao.setMeta(retrieve(keyword), META_KEY_NAME, keyword.getName()));
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \_________________ */

    private List<Keyword> toKeyword(Collection<Ii> keywordsIi) {
        List<Keyword> result = new ArrayList<Keyword>();
        for (Ii ii : keywordsIi) {
            result.add(toKeyword(ii));
        }
        return result;
    }

    private Keyword toKeyword(Ii keywordIi) {
        return new Keyword(keywordIi.getUUID(), getName(keywordIi));
    }

    private String getName(Ii item) {
        return itemWithMeta(dao, item).getMeta(META_KEY_NAME);
    }

    private Ii retrieve(Keyword keyword) throws NotFoundException {
        Ii item = dao.load(keyword.getUuid());
        if (item == null) {
            throw new NotFoundException(keyword.getName());
        } else {
            return item;
        }
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
