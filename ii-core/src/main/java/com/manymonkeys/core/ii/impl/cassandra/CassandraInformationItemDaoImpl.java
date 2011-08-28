package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.InformationItemDao;
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
public class CassandraInformationItemDaoImpl{//implements InformationItemDao {

    final static Logger logger = LoggerFactory.getLogger(CassandraInformationItemDaoImpl.class);

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
     * THis is done because each item has to have at leas some meta information
     * Otherwise we can not distinct non-existent item from item with no meta
     */
    private static final String META_KEY_CREATOR = "CREATED BY";

    /**
     * Keyspace in Cassandra to store data
     */
    private Keyspace keyspace;

    // Column families constants
    private static final String CF_META = "META";
    private static final String CF_META_INDEX = "META_INDEX";
    private static final String CF_META_PREFIX = "META_PREFIX";
    private static final String CF_COMPONENTS = "COMPONENTS";
    private static final String CF_PARENTS = "PARENTS";

    private static final UUIDSerializer us = UUIDSerializer.get();
    private static final DoubleSerializer ds = DoubleSerializer.get();
    private static final StringSerializer ss = StringSerializer.get();


    // Currently not used
//    private static final String CF_SIMILAR = "SIMILAR";
//    private static final String CF_RECALCULATE = "RECALCULATE";

    public CassandraInformationItemDaoImpl(Keyspace keyspace) {
        this.keyspace = keyspace;
    }

    public CassandraIiImpl createInformationItem() {
        UUID uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        CassandraIiImpl item = createInformationItem(uuid);
        setMeta(item, META_KEY_CREATOR, this.getClass().getName());
        return item;
    }

    CassandraIiImpl createInformationItem(UUID uuid) {
        return new CassandraIiImpl(uuid);
    }

    protected void deleteInformationItem(Ii item) {
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

    //Todo Anton Chebotaev - wtf is this method for in stateless class?
    public void reloadMetadata(Collection<Ii> items) {
        if (items.isEmpty())
            return;

        logger.debug(String.format("reloadMetadata(%d) called", items.size()));
        long startTime = System.currentTimeMillis();

        Collection<UUID> ids = getUniqueIds(items);

        MultigetSliceQuery<UUID, String, String> query = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
        query.setColumnFamily(CF_META);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, String, String>> queryResult = query.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        for (Ii item : items) {
            if (!(item instanceof CassandraIiImpl))
                continue;
            CassandraIiImpl itemImpl = (CassandraIiImpl) item;
            itemImpl.meta = new HashMap<String, String>();
            for (HColumn<String, String> column : rows.getByKey(itemImpl.uuid).getColumnSlice().getColumns()) {
                itemImpl.meta.put(column.getName(), column.getValue());
            }
        }

        logger.debug(String.format("reloadMetadata(%d) got result in %d seconds", items.size(), (System.currentTimeMillis() - startTime) / 1000));
    }

    //Todo Anton Chebotaev - wtf is this method for in stateless class?
    public Collection<Ii> reloadComponents(Collection<Ii> items) {
        if (items.isEmpty())
            return Collections.emptySet();

        Collection<UUID> ids = getUniqueIds(items);

        logger.debug(String.format("reloadComponents(%d) called", items.size()));
        long startTime = System.currentTimeMillis();

        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_COMPONENTS);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();

        Set<Ii> result = new HashSet<Ii>();
        for (Ii item : items) {
            if (!(item instanceof CassandraIiImpl))
                continue;
            CassandraIiImpl itemImpl = (CassandraIiImpl) item;
            itemImpl.components = new HashMap<Ii, Double>();
            for (HColumn<UUID, Double> column : rows.getByKey(itemImpl.uuid).getColumnSlice().getColumns()) {
                CassandraIiImpl component = createInformationItem(column.getName());
                result.add(component);
                itemImpl.components.put(component, column.getValue());
            }
        }

        logger.debug(String.format("reloadComponents(%d) got result in %d seconds", items.size(), (System.currentTimeMillis() - startTime) / 1000));
        return result;
    }

    @OwledMethod
    public Collection<Ii> reloadParents(@OwledArgument Collection<Ii> items) {
        if (items.isEmpty())
            return Collections.emptySet();

        Collection<UUID> ids = getUniqueIds(items);

        System.out.println("FUCK");

        logger.debug(String.format("reloadParents(%d) called", items.size()));
        long startTime = System.currentTimeMillis();

        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_PARENTS);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();

        Set<Ii> result = new HashSet<Ii>();
        for (Ii item : items) {
            if (!(item instanceof CassandraIiImpl))
                continue;
            CassandraIiImpl itemImpl = (CassandraIiImpl) item;
            itemImpl.parents = new HashMap<Ii, Double>();
            for (HColumn<UUID, Double> column : rows.getByKey(itemImpl.uuid).getColumnSlice().getColumns()) {
                CassandraIiImpl component = createInformationItem(column.getName());
                result.add(component);
                itemImpl.parents.put(component, column.getValue());
            }
        }

        logger.debug(String.format("reloadParents(%d) got result in %d seconds", items.size(), (System.currentTimeMillis() - startTime) / 1000));
        return result;
    }

    @OwledMethod
    public Ii loadByUUID(UUID uuid) {

        SliceQuery<UUID, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, us, ss, ss);
        sliceQuery.setColumnFamily(CF_META);
        sliceQuery.setKey(uuid);
        sliceQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<String, String>> queryResult = sliceQuery.execute();
        List<HColumn<String, String>> columns = queryResult.get().getColumns();

        if (columns.isEmpty())
            return null; //TODO: means meta can't be loaded without meta; discuss

        CassandraIiImpl item = createInformationItem(uuid);
        for (HColumn<String, String> column : columns)
            item.meta.put(column.getName(), column.getValue());

        return item;
    }

    @OwledMethod
    public Collection<Ii> loadByUUIDs(Collection<UUID> uuids) {

        logger.debug(String.format("loadByUUIDs(%d) called", uuids.size()));
        long startTime = System.currentTimeMillis();

        MultigetSliceQuery<UUID, String, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
        multigetSliceQuery.setColumnFamily(CF_META);
        multigetSliceQuery.setKeys(uuids);
        multigetSliceQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, String, String>> queryResult = multigetSliceQuery.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        List<Ii> result = new LinkedList<Ii>();
        for (Row<UUID, String, String> row : rows) {
            List<HColumn<String, String>> columns = row.getColumnSlice().getColumns();
            if (columns.isEmpty())
                continue;  //TODO: means meta can't be loaded without meta; discuss

            CassandraIiImpl item = createInformationItem(row.getKey());
            for (HColumn<String, String> column : columns)
                item.meta.put(column.getName(), column.getValue());

            result.add(item);
        }

        logger.debug(String.format("loadByUUIDs(%d) got result in %d seconds", result.size(), (System.currentTimeMillis() - startTime) / 1000));
        return result;
    }


    protected void setComponentWeight(Ii item, Ii component, Double weight) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();
        CassandraIiImpl localItem = (CassandraIiImpl) item;
        CassandraIiImpl localComponent = (CassandraIiImpl) component;

        if (localItem.components != null)
            localItem.components.put(component, weight);
        if (localComponent.parents != null)
            localComponent.parents.put(item, weight);

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, us);
        mutator.addInsertion(item.getUUID(), CF_COMPONENTS, HFactory.createColumn(component.getUUID(), weight, us, ds));
        mutator.addInsertion(component.getUUID(), CF_PARENTS, HFactory.createColumn(item.getUUID(), weight, us, ds));
        mutator.execute();

        //TODO: in case of precalculated recommendations add all parents of component to recalculate queue
    }

    protected void removeComponent(Ii item, Ii component) {
        if (!(item instanceof CassandraIiImpl && component instanceof CassandraIiImpl))
            throw generateWrongDaoException();
        CassandraIiImpl localItem = (CassandraIiImpl) item;
        CassandraIiImpl localComponent = (CassandraIiImpl) component;

        if (localItem.components != null)
            localItem.components.remove(component);
        if (localComponent.parents != null)
            localComponent.parents.remove(item);

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, us);
        mutator.addDeletion(item.getUUID(), CF_COMPONENTS, component.getUUID(), us);
        mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), us);
        mutator.execute();

    }

    protected void setMeta(Ii item, String key, String value) {
        setMeta(item, key, value, false);
    }

    protected void setMeta(Ii item, String key, String value, boolean isIndexed) {
        if (!(item instanceof CassandraIiImpl))
            throw generateWrongDaoException();
        CassandraIiImpl localItem = ((CassandraIiImpl) item);

        // Update data
        HFactory.createMutator(keyspace, us).insert(item.getUUID(), CF_META, HFactory.createStringColumn(key, value));

        // Update index
        String oldValue = localItem.meta.get(key);
        Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
        removeMetaIndex(localItem, key, oldValue, mutator);
        addMetaIndex(localItem, key, value, mutator, isIndexed);
        mutator.execute();

        // Update model
        localItem.meta.put(key, value);
    }

    protected void removeMeta(Ii item, String key) {
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
        localItem.meta.remove(key);
    }

    protected Collection<Ii> loadByMeta(String key, String value) {

        String queryKey = String.format(META_FORMAT, key, value);

        SliceQuery<String, UUID, Double> query = HFactory.createSliceQuery(keyspace, ss, us, ds)
                .setColumnFamily(CF_META_INDEX)
                .setKey(queryKey)
                .setRange(null, null, false, MULTIGET_COUNT);
        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> columns = queryResult.get();

        Collection<UUID> result = new LinkedList<UUID>();
        for (HColumn<UUID, Double> column : columns.getColumns()) {
            result.add(column.getName());
        }
        return loadByUUIDs(result);
    }

    protected Map<UUID, String> searchByMetaPrefix(String key, String prefix) {

        String queryKey = String.format(META_FORMAT, key, prefix.toLowerCase());

        SliceQuery<String, UUID, String> query = HFactory.createSliceQuery(keyspace, ss, us, ss)
                .setColumnFamily(CF_META_PREFIX)
                .setKey(queryKey)
                .setRange(null, null, false, SEARCH_COUNT);
        QueryResult<ColumnSlice<UUID, String>> queryResult = query.execute();
        ColumnSlice<UUID, String> columns = queryResult.get();

        Map<UUID, String> result = new HashMap<UUID, String>();
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

    private UnsupportedOperationException generateWrongDaoException() {
        return new UnsupportedOperationException("This dao can't operate with this item");
    }

    private void addMetaIndex(CassandraIiImpl item, String key, String value, Mutator<String> mutator, boolean isIndexed) {
        String rowKey = String.format(META_FORMAT, key, value);
        mutator.addInsertion(rowKey, CF_META_INDEX, HFactory.createColumn(item.getUUID(), 1D, us, ds));
        if (isIndexed) {
            String[] words = value.toLowerCase().split("\\s");
            for (String word : words) {
                for (int i = 1; i <= word.length(); i++) {
                    String rowName = String.format(META_FORMAT, key, word.substring(0, i));
                    mutator.addInsertion(rowName, CF_META_PREFIX, HFactory.createColumn(item.getUUID(), value, us, ss));
                }
            }
        }
    }

    private void removeMetaIndex(CassandraIiImpl item, String key, String oldValue, Mutator<String> mutator) {
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

}
