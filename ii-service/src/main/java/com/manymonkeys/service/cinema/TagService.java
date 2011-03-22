package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.impl.neo4j.Neo4jInformationItemDaoImpl;
import org.neo4j.graphdb.Transaction;

import java.util.NoSuchElementException;

/**
 * Many Monkeys
 * 
 * @author Anton Chebotaev
 */
public class TagService extends Neo4jInformationItemDaoImpl {

    public static final String NAME = TagService.class.getName() + ".NAME";

    public static final String TAG_CLASS_NAME = TagService.class.getName() + ".TAG_CLASS";

    public InformationItem createTag(String name) {
        Transaction tx = beginTransaction();
        try {

            InformationItem item = createInformationItem();
            setItemClass(item, TAG_CLASS_NAME);
            setMeta(item, NAME, name);

            tx.success();
            return item;
        } finally {
            tx.finish();
        }
    }

    public void setName(InformationItem item, String name) {
        setMeta(item, NAME, name);
    }

    public InformationItem getTag(String name) {
        try {
            return getByMeta(NAME, String.format("\"%s\"", name)).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

}
