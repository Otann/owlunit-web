package com.manymonkeys.service.cinema;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.impl.cassandra.CassandraInformationItemDaoImpl;
import com.manymonkeys.security.shiro.annotation.OwledMethod;
import me.prettyprint.hector.api.Keyspace;

import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class TagService extends CassandraInformationItemDaoImpl {

    public static final String CLASS_MARK_KEY = TagService.class.getName();
    public static final String CLASS_MARK_VALUE = "#"; // this indicates that item was created through TagService class

    public static final String NAME = TagService.class.getName() + ".NAME";

    public TagService(Keyspace keyspace) {
        super(keyspace);
    }

    public InformationItem createTag(String name) {
        InformationItem item = createInformationItem();
        setMeta(item, NAME, name, true);
        setMeta(item, CLASS_MARK_KEY, CLASS_MARK_VALUE);
        return item;
    }

    public InformationItem getTag(String name) {
        try {
            return loadByMeta(NAME, name).iterator().next();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public Collection<InformationItem> getAll() {
        return loadByMeta(CLASS_MARK_KEY, CLASS_MARK_VALUE);
    }

}
