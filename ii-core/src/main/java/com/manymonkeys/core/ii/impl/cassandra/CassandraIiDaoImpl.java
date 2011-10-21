package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.security.shiro.annotation.OwledArgument;
import com.manymonkeys.security.shiro.annotation.OwledMethod;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraIiDaoImpl implements IiDao {

    final static Logger logger = LoggerFactory.getLogger(CassandraIiDaoImpl.class);

    /**
     * Miltiget queries in Cassandra requires to set either query sizelimit or returnKeysOnly flag.
     * So when we need actual data from rows we have to determine limit of the query.
     * This affects querying metadata, components and parents
     */
    public static final int MULTIGET_COUNT = 10000;

    /**
     * Limits search results
     */
    public static final int SEARCH_COUNT = 100;

    /**
     * Formats for keys in META_INDEX column family
     */
    private static final String META_FORMAT = "%s#%s";

    /**
     * Each item has mark of it's creator
     * This is done because each item has to have at leas some meta information to be persisted
     * Otherwise we can not distinct non-existent item from item with no data
     */
    private static final String META_KEY_CREATOR = "CREATED BY";

    /////////////////////
    // Column Families
    /////////////////////

    private static final String CF_META = "META";
    private static final String CF_META_INDEX = "META_INDEX";
    private static final String CF_META_PREFIX = "META_PREFIX";

    private static final String CF_COMPONENTS = "COMPONENTS";
    private static final String CF_PARENTS = "PARENTS";

    private static final UUIDSerializer   us = UUIDSerializer.get();
    private static final DoubleSerializer ds = DoubleSerializer.get();
    private static final StringSerializer ss = StringSerializer.get();


    /**
     * Keyspace in Cassandra to store data
     */
    private Keyspace keyspace;

    public CassandraIiDaoImpl(Keyspace keyspace) {
        this.keyspace = keyspace;
    }

    public CassandraIiImpl createInformationItem() {
        UUID uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        CassandraIiImpl item = new CassandraIiImpl(uuid);
        setMeta(item, META_KEY_CREATOR, this.getClass().getName());
        return item;
    }

    public void deleteInformationItem(Ii item) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();
        CassandraIiImpl localItem = (CassandraIiImpl) item;

        Mutator<UUID> uuidMutator = HFactory.createMutator(keyspace, us);
        for (Ii parent : item.getParents().keySet())
            uuidMutator.addDeletion(parent.getUUID(), CF_COMPONENTS, localItem.uuid, us);
        for (Ii component : item.getComponents().keySet())
            uuidMutator.addDeletion(component.getUUID(), CF_PARENTS, localItem.uuid, us);
        uuidMutator.addDeletion(localItem.uuid, CF_META, null, ss);
        uuidMutator.addDeletion(localItem.uuid, CF_COMPONENTS, null, ss);
        uuidMutator.addDeletion(localItem.uuid, CF_PARENTS, null, ss);
        uuidMutator.execute();

        Mutator<String> stringMutator = HFactory.createMutator(keyspace, ss);
        for (String key : localItem.meta.keySet()) {
            String oldValue = localItem.meta.get(key);
            removeMetaIndex(localItem, key, oldValue, stringMutator);
        }
        stringMutator.execute();
    }

    @Override
    public Ii loadMetadata(Ii item) {
        SliceQuery<UUID, String, String> query = HFactory.createSliceQuery(keyspace, us, ss, ss);
        query.setColumnFamily(CF_META);
        query.setKey(item.getUUID());
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<String, String>> queryResult = query.execute();
        ColumnSlice<String, String> slice = queryResult.get();

        return updateMetadata(item, slice.getColumns());
    }

    public Collection<Ii> loadMetadata(Collection<Ii> items) {
        if (items.isEmpty())
            return items;

        Collection<Ii> result = new LinkedList<Ii>();
        Collection<UUID> ids = getUniqueIds(items);

        MultigetSliceQuery<UUID, String, String> query = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
        query.setColumnFamily(CF_META);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, String, String>> queryResult = query.execute();
        Rows<UUID, String, String> rows = queryResult.get();
        logger.debug(String.format("loadMetadata(%d) called, query took %d milliseconds", items.size(), queryResult.getExecutionTimeMicro() / 1000));

        for (Ii item : items) {
            result.add(updateMetadata(item, rows.getByKey(item.getUUID()).getColumnSlice().getColumns()));
        }
        return result;
    }

    @Override
    public Ii loadComponents(Ii item) {
        SliceQuery<UUID, UUID, Double> query = HFactory.createSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_COMPONENTS);
        query.setKey(item.getUUID());
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> slice = queryResult.get();

        return updateComponents(item, slice.getColumns());
    }

    public Collection<Ii> loadComponents(Collection<Ii> items) {
        if (items.isEmpty())
            return Collections.emptySet();

        Collection<Ii> result = new LinkedList<Ii>();
        Collection<UUID> ids = getUniqueIds(items);

        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_COMPONENTS);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();
        logger.debug(String.format("loadComponents(%d) called, query took %d milliseconds", items.size(), queryResult.getExecutionTimeMicro() / 1000));

        for (Ii item : items) {
            result.add(updateComponents(item, rows.getByKey(item.getUUID()).getColumnSlice().getColumns()));
        }
        return result;
    }

    @Override
    public Ii loadParents(Ii item) {
        SliceQuery<UUID, UUID, Double> query = HFactory.createSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_PARENTS);
        query.setKey(item.getUUID());
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> slice = queryResult.get();

        return updateParents(item, slice.getColumns());
    }

    @OwledMethod
    public Collection<Ii> loadParents(@OwledArgument Collection<Ii> items) {
        if (items.isEmpty())
            return Collections.emptySet();

        Collection<Ii> result = new LinkedList<Ii>();
        Collection<UUID> ids = getUniqueIds(items);

        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_PARENTS);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();
        logger.debug(String.format("loadParents(%d) called, query took %d milliseconds", items.size(), queryResult.getExecutionTimeMicro() / 1000));

        for (Ii item : items) {
            result.add(updateParents(item, rows.getByKey(item.getUUID()).getColumnSlice().getColumns()));
        }
        return result;
    }

    @OwledMethod
    public Ii load(UUID uuid) {

        ColumnQuery<UUID, String, String> query = HFactory.createColumnQuery(keyspace, us, ss, ss);
        query.setColumnFamily(CF_META);
        query.setKey(uuid);
        query.setName(META_KEY_CREATOR);

        QueryResult<HColumn<String, String>> queryResult = query.execute();
        HColumn<String, String> column = queryResult.get();

        if (column == null) {
            return null;
        } else {
            return new CassandraIiImpl(uuid);
        }
    }

    @OwledMethod
    public Collection<Ii> load(@OwledArgument Collection<UUID> uuids) {

        MultigetSliceQuery<UUID, String, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
        multigetSliceQuery.setColumnFamily(CF_META);
        multigetSliceQuery.setKeys(uuids);
        multigetSliceQuery.setRange(null, null, false, 1);

        QueryResult<Rows<UUID, String, String>> queryResult = multigetSliceQuery.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        List<Ii> result = new LinkedList<Ii>();
        for (Row<UUID, String, String> row : rows) {
            result.add(new CassandraIiImpl(row.getKey()));
        }

        logger.debug(String.format("load(%d) called, query took %d milliseconds", uuids.size(), queryResult.getExecutionTimeMicro() / 1000));

        return result;
    }


    public Ii setComponentWeight(Ii item, Ii component, Double weight) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, us);
        mutator.addInsertion(item.getUUID(), CF_COMPONENTS, HFactory.createColumn(component.getUUID(), weight, us, ds));
        mutator.addInsertion(component.getUUID(), CF_PARENTS, HFactory.createColumn(item.getUUID(), weight, us, ds));
        mutator.execute();

        if (item.getComponents() == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl((CassandraIiImpl) item);
            newItem.components.put(component, weight);
            return newItem;
        }
    }

    public Ii removeComponent(Ii item, Ii component) {
        if (!(item instanceof CassandraIiImpl && component instanceof CassandraIiImpl))
            throw generateWrongDaoException();

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, us);
        mutator.addDeletion(item.getUUID(), CF_COMPONENTS, component.getUUID(), us);
        mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), us);
        mutator.execute();

        if (item.getComponents() == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl((CassandraIiImpl) item);
            newItem.components.remove(component);
            return newItem;
        }
    }

    public Ii setMeta(Ii item, String key, String value) {
        return setMetaExtended(item, key, value, true);
    }

    public Ii setUnindexedMeta(Ii item, String key, String value) {
        return setMetaExtended(item, key, value, false);
    }

    private Ii setMetaExtended(Ii item, String key, String value, boolean isIndexed) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();
        String oldValue;
        if (item.getMetaMap() != null) {
            oldValue = item.getMeta(key);
        } else {
            oldValue = getMeta(item, key);
        }

        // Update data
        HFactory.createMutator(keyspace, us).insert(item.getUUID(), CF_META, HFactory.createStringColumn(key, value));

        // Update index
        Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
        if (oldValue != null) {
            removeMetaIndex(item, key, oldValue, mutator);
        }
        if (isIndexed) {
            addMetaIndex(item, key, value, mutator);
        }
        mutator.execute();

        // Update model
        if (item.getMetaMap() == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl((CassandraIiImpl) item);
            newItem.meta = new HashMap<String, String>(item.getMetaMap());
            newItem.meta.put(key, value);
            return item;
        }
    }

    public Ii removeMeta(Ii item, String key) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();
        CassandraIiImpl localItem = (CassandraIiImpl) item;

        // Update data
        HFactory.createMutator(keyspace, us).delete(item.getUUID(), CF_META, key, ss);

        // Update index
        String oldValue = localItem.meta.get(key);
        Mutator<String> stringMutator = HFactory.createMutator(keyspace, ss);
        removeMetaIndex(localItem, key, oldValue, stringMutator);
        stringMutator.execute();

        // Update model
        if (item.getMetaMap() == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl(localItem);
            newItem.meta = new HashMap<String, String>(localItem.meta);
            newItem.meta.remove(key);
            return item;
        }
    }

    public Collection<Ii> load(String key, String value) {

        String queryKey = String.format(META_FORMAT, key, value);

        SliceQuery<String, UUID, Double> query = HFactory.createSliceQuery(keyspace, ss, us, ds)
                .setColumnFamily(CF_META_INDEX)
                .setKey(queryKey)
                .setRange(null, null, false, MULTIGET_COUNT);
        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> columns = queryResult.get();

        Collection<UUID> uuids = new LinkedList<UUID>();
        for (HColumn<UUID, Double> column : columns.getColumns()) {
            uuids.add(column.getName());
        }

        return load(uuids);
    }

    public Map<UUID, String> search(String key, String prefix) {

        Map<UUID, String> result = new HashMap<UUID, String>();

        String queryKey = String.format(META_FORMAT, key, prefix.toLowerCase());
        SliceQuery<String, UUID, String> query = HFactory.createSliceQuery(keyspace, ss, us, ss)
                .setColumnFamily(CF_META_PREFIX)
                .setKey(queryKey)
                .setRange(null, null, false, SEARCH_COUNT);
        QueryResult<ColumnSlice<UUID, String>> queryResult = query.execute();
        ColumnSlice<UUID, String> columns = queryResult.get();

        for (HColumn<UUID, String> column : columns.getColumns()) {
            result.put(column.getName(), column.getValue());
        }

        return result;
    }

    /*------------------\
    |   P R I V A T E   |
    \__________________*/

    private Collection<UUID> getUniqueIds(Collection<Ii> items) {
        Set<UUID> result = new HashSet<UUID>();
        for (Ii item : items) {
            CassandraIiImpl local = (CassandraIiImpl) item;
            result.add(local.uuid);
        }
        return result;
    }

    private Map<UUID, Ii> toMap(Collection<Ii> items) {
        Map<UUID, Ii> result = new HashMap<UUID, Ii>();
        for (Ii item : items) {
            result.put(item.getUUID(), item);
        }
        return result;
    }

    private UnsupportedOperationException generateWrongDaoException() {
        return new UnsupportedOperationException("This dao can't operate with this item");
    }

    private Ii updateMetadata(Ii item, Collection<HColumn<String, String>> columns) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();

        CassandraIiImpl newItem = new CassandraIiImpl((CassandraIiImpl) item);
        newItem.meta = new HashMap<String, String>();

        for (HColumn<String, String> column : columns) {
            newItem.meta.put(column.getName(), column.getValue());
        }

        return newItem;
    }

    private Ii updateComponents(Ii item, Collection<HColumn<UUID, Double>> columns) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();

        Map<UUID, Ii> oldComponents = toMap(item.getComponents().keySet());

        CassandraIiImpl newItem;
        if (item.getComponents() == null) {
            newItem = (CassandraIiImpl) item;
            if (!newItem.components.isEmpty()) {
                newItem.components = new HashMap<Ii, Double>();
            }
        } else {
            newItem = new CassandraIiImpl((CassandraIiImpl) item);
        }

        for (HColumn<UUID, Double> column : columns) {
            UUID componentId = column.getName();
            Double weight = column.getValue();

            Ii component;
            if (oldComponents.containsKey(componentId)) {
                component = oldComponents.get(componentId);
            } else {
                component = new CassandraIiImpl(componentId);
            }

            newItem.components.put(component, weight);
        }

        return newItem;
    }

    private Ii updateParents(Ii item, Collection<HColumn<UUID, Double>> columns) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();

        Map<UUID, Ii> oldParents = toMap(item.getParents().keySet());

        CassandraIiImpl newItem;
        if (item.getComponents() == null) {
            newItem = (CassandraIiImpl) item;
            if (!newItem.parents.isEmpty()) {
                newItem.parents = new HashMap<Ii, Double>();
            }
        } else {
            newItem = new CassandraIiImpl((CassandraIiImpl) item);
        }

        for (HColumn<UUID, Double> column : columns) {
            UUID parentId = column.getName();
            Double weight = column.getValue();

            Ii parent;
            if (oldParents.containsKey(parentId)) {
                parent = oldParents.get(parentId);
            } else {
                parent = new CassandraIiImpl(parentId);
            }

            newItem.parents.put(parent, weight);
        }
        return newItem;
    }

    private void addMetaIndex(Ii item, String key, String value, Mutator<String> mutator) {
        String rowKey = String.format(META_FORMAT, key, value);
        mutator.addInsertion(rowKey, CF_META_INDEX, HFactory.createColumn(item.getUUID(), 1D, us, ds));
        String[] words = value.toLowerCase().split("\\s");
        for (String word : words) {
            for (int i = 1; i <= word.length(); i++) {
                String rowName = String.format(META_FORMAT, key, word.substring(0, i));
                mutator.addInsertion(rowName, CF_META_PREFIX, HFactory.createColumn(item.getUUID(), value, us, ss));
            }
        }
    }

    private void removeMetaIndex(Ii item, String key, String oldValue, Mutator<String> mutator) {
        if (oldValue == null || key == null)
            return;
        String rowKey = String.format(META_FORMAT, key, oldValue);
        mutator.addDeletion(rowKey, CF_META_INDEX, item.getUUID(), us);
        String[] words = oldValue.toLowerCase().split("\\s");
        for (String word : words) {
            for (int i = 1; i <= word.length(); i++) {
                String rowName = String.format(META_FORMAT, key, word.substring(0, i));
                mutator.addDeletion(rowName, CF_META_PREFIX, item.getUUID(), us);
            }
        }
    }

    private String getMeta(Ii ii, String key) {
        ColumnQuery<UUID, String, String> query = HFactory.createColumnQuery(keyspace, us, ss, ss);
        query.setColumnFamily(CF_META);
        query.setKey(ii.getUUID());
        query.setName(key);

        QueryResult<HColumn<String, String>> queryResult = query.execute();
        if (queryResult.get() == null) {
            return null;
        } else {
            return queryResult.get().getValue();
        }
    }
}
