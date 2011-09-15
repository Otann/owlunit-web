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
        return new Keyword(
                item.getUUID(),
                meta.getMeta(META_KEY_NAME)
        );
    }

    static Ii keywordToIi(IiDao dao, Keyword keyword) throws NotFoundException {
        Ii item = dao.load(keyword.getUuid());
        if (item == null) {
            throw new NotFoundException(String.format("Keyword(%s)", keyword.getName()));
        } else {
            return item;
        }
    }

    @Override
    public Keyword createKeyword(String name) {
        Ii item = dao.createInformationItem();
        dao.setUnindexedMeta(item, CLASS_MARK_KEY, CLASS_MARK_VALUE);
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
        Collection<Ii> blankItems = dao.load(META_KEY_NAME, name);
        if (blankItems.isEmpty()) {
            throw new NotFoundException(String.format("Keyword(%s)", name));
        } else {
            Ii meta = dao.loadMetadata(blankItems.iterator().next());
            return iiToKeyword(dao, meta);
        }
    }

    @Override
    public List<Keyword> listKeywords() {
        Collection<Ii> blankItems = dao.load(CLASS_MARK_KEY, CLASS_MARK_VALUE);
        return iisToKeywords(dao.loadMetadata(blankItems));
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
