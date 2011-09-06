package com.manymonkeys.service.cinema.impl;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.model.cinema.Keyword;
import com.manymonkeys.service.cinema.KeywordService;
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

    public Keyword createKeyword(String name) {
        Ii item = dao.createInformationItem();
        dao.setUnindexedMeta(item, CLASS_MARK_KEY, CLASS_MARK_VALUE);

        dao.setMeta(item, META_KEY_NAME, name);
        return toDomainClass(item);
    }

    public Keyword loadKeyword(UUID uuid) {
        return toDomainClass(dao.load(uuid));
    }

    public Keyword loadKeyword(String name) {
        //TODO Anton Chebotaev - discuss if we have unique tags or not
        Collection<Ii> blankItems = dao.load(META_KEY_NAME, name);
        if (blankItems.isEmpty()) {
            return null;
        } else {
            return toDomainClass(dao.loadMetadata(blankItems).iterator().next());
        }
    }

    public List<Keyword> listKeywords() {
        Collection<Ii> blankItems = dao.load(CLASS_MARK_KEY, CLASS_MARK_VALUE);
        return toDomainClass(dao.loadMetadata(blankItems));
    }


    public Keyword updateName(Keyword keyword, String name) {
        return toDomainClass(dao.setMeta(retrieve(keyword), META_KEY_NAME, name));
    }

    public Boolean isKeyword(Keyword keyword) {
        return itemWithMeta(dao, retrieve(keyword)).getMeta(CLASS_MARK_KEY) != null;
    }

    /*-- - - - - - - - -\
    |   P R I V A T E   |
    \_________________ */

    private List<Keyword> toDomainClass(Collection<Ii> keywordsIi) {
        List<Keyword> result = new ArrayList<Keyword>();
        for (Ii ii : keywordsIi) {
            result.add(toDomainClass(ii));
        }

        return result;
    }

    private Keyword toDomainClass(Ii keywordIi) {
        return new Keyword(getName(keywordIi), keywordIi.getUUID());
    }

    private String getName(Ii item) {
        return itemWithMeta(dao, item).getMeta(META_KEY_NAME);
    }

    private Ii retrieve(Keyword keyword) {
        return dao.load(keyword.getUuid());
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
