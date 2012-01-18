package com.manymonkeys.service.impl;

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

import static com.manymonkeys.service.impl.util.Utils.itemWithMeta;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
public class KeywordServiceImpl implements KeywordService {

    @Autowired
    protected IiDao dao;

    private static final String CLASS_MARK_KEY = KeywordServiceImpl.class.getName();
    private static final String CLASS_MARK_VALUE = "#";

    static final String META_KEY_NAME = CLASS_MARK_KEY + ".NAME";

    static Keyword iiToKeyword(IiDao dao, Ii item) {
        Ii meta = itemWithMeta(dao, item);
        if (!meta.getMeta(CLASS_MARK_KEY).equals(CLASS_MARK_VALUE)) {
            throw new IllegalArgumentException("This is not a keyword");
        }
        return new Keyword(
                item.getUUID(),
                meta.getMeta(META_KEY_NAME)
        );
    }

    static Ii keywordToIi(IiDao dao, Keyword keyword) throws NotFoundException {
        if (keyword.getUuid() == null) {
            throw new IllegalArgumentException("You have to create keyword first");
        }
        Ii item = dao.load(keyword.getUuid());
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
    public Keyword loadByUUID(UUID uuid) throws NotFoundException {
        Ii item = dao.load(uuid);
        if (item == null) {
            throw new NotFoundException(String.format("Keyword(%s)", uuid.toString()));
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
