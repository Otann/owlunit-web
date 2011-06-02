package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;
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

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraInformationItemDaoImpl implements InformationItemDao {

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

    @Override
    public CassandraInformationItemImpl createInformationItem() {
        UUID uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        CassandraInformationItemImpl item = createInformationItem(uuid);
        setMeta(item, META_KEY_CREATOR, this.getClass().getName());
        return item;
    }

    CassandraInformationItemImpl createInformationItem(UUID uuid) {
        return new CassandraInformationItemImpl(uuid);
    }

    @Override
    public void deleteInformationItem(InformationItem item) {
        if (!(item instanceof CassandraInformationItemImpl))
            throw generateWrongDaoException();
        CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;

        Mutator<UUID> uuidMutator = HFactory.createMutator(keyspace, us);
        for (InformationItem parent : item.getParents().keySet())
            uuidMutator.addDeletion(parent.getUUID(), CF_COMPONENTS, localItem.uuid, us);
        for (InformationItem component : item.getComponents().keySet())
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
    public void reloadMetadata(Collection<InformationItem> items) {
        if (items.isEmpty())
            return;

        Collection<UUID> ids = getUniqueIds(items);

        MultigetSliceQuery<UUID, String, String> query = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
        query.setColumnFamily(CF_META);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, String, String>> queryResult = query.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        for (InformationItem item : items) {
            if (!(item instanceof CassandraInformationItemImpl))
                continue;
            CassandraInformationItemImpl itemImpl = (CassandraInformationItemImpl) item;
            itemImpl.meta = new HashMap<String, String>();
            for (HColumn<String, String> column : rows.getByKey(itemImpl.uuid).getColumnSlice().getColumns()) {
                itemImpl.meta.put(column.getName(), column.getValue());
            }
        }
    }

    @Override
    public Collection<InformationItem> reloadComponents(Collection<InformationItem> items) {
        if (items.isEmpty())
            return Collections.emptySet();

        Collection<UUID> ids = getUniqueIds(items);

        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_COMPONENTS);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();

        Set<InformationItem> result = new HashSet<InformationItem>();
        for (InformationItem item : items) {
            if (!(item instanceof CassandraInformationItemImpl))
                continue;
            CassandraInformationItemImpl itemImpl = (CassandraInformationItemImpl) item;
            itemImpl.components = new HashMap<InformationItem, Double>();
            for (HColumn<UUID, Double> column : rows.getByKey(itemImpl.uuid).getColumnSlice().getColumns()) {
                CassandraInformationItemImpl component = createInformationItem(column.getName());
                result.add(component);
                itemImpl.components.put(component, column.getValue());
            }
        }
        return result;
    }

    @Override
    public Collection<InformationItem> reloadParents(Collection<InformationItem> items) {
        if (items.isEmpty())
            return Collections.emptySet();

        Collection<UUID> ids = getUniqueIds(items);

        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(CF_PARENTS);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();

        Set<InformationItem> result = new HashSet<InformationItem>();
        for (InformationItem item : items) {
            if (!(item instanceof CassandraInformationItemImpl))
                continue;
            CassandraInformationItemImpl itemImpl = (CassandraInformationItemImpl) item;
            itemImpl.parents = new HashMap<InformationItem, Double>();
            for (HColumn<UUID, Double> column : rows.getByKey(itemImpl.uuid).getColumnSlice().getColumns()) {
                CassandraInformationItemImpl component = createInformationItem(column.getName());
                result.add(component);
                itemImpl.parents.put(component, column.getValue());
            }
        }
        return result;
    }

    @Override
    public InformationItem loadByUUID(UUID uuid) {

        SliceQuery<UUID, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, us, ss, ss);
        sliceQuery.setColumnFamily(CF_META);
        sliceQuery.setKey(uuid);
        sliceQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<String, String>> queryResult = sliceQuery.execute();
        List<HColumn<String, String>> columns = queryResult.get().getColumns();

        if (columns.isEmpty())
            return null; //TODO: means meta can't be loaded without meta; discuss

        CassandraInformationItemImpl item = createInformationItem(uuid);
        for (HColumn<String, String> column : columns)
            item.meta.put(column.getName(), column.getValue());

        return item;
    }

    @Override
    public Collection<InformationItem> loadByUUIDs(Collection<UUID> uuids) {

        MultigetSliceQuery<UUID, String, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
        multigetSliceQuery.setColumnFamily(CF_META);
        multigetSliceQuery.setKeys(uuids);
        multigetSliceQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, String, String>> queryResult = multigetSliceQuery.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        List<InformationItem> result = new LinkedList<InformationItem>();
        for (Row<UUID, String, String> row : rows) {
            List<HColumn<String, String>> columns = row.getColumnSlice().getColumns();
            if (columns.isEmpty())
                continue;  //TODO: means meta can't be loaded without meta; discuss

            CassandraInformationItemImpl item = createInformationItem(row.getKey());
            for (HColumn<String, String> column : columns)
                item.meta.put(column.getName(), column.getValue());

            result.add(item);
        }

        return result;
    }


    @Override
    public void setComponentWeight(InformationItem item, InformationItem component, Double weight) {
        if (!(item instanceof CassandraInformationItemImpl))
            throw generateWrongDaoException();
        CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;
        CassandraInformationItemImpl localComponent = (CassandraInformationItemImpl) component;

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

    @Override
    public void removeComponent(InformationItem item, InformationItem component) {
        if (!(item instanceof CassandraInformationItemImpl && component instanceof CassandraInformationItemImpl))
            throw generateWrongDaoException();
        CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;
        CassandraInformationItemImpl localComponent = (CassandraInformationItemImpl) component;

        if (localItem.components != null)
            localItem.components.remove(component);
        if (localComponent.parents != null)
            localComponent.parents.remove(item);

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, us);
        mutator.addDeletion(item.getUUID(), CF_COMPONENTS, component.getUUID(), us);
        mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), us);
        mutator.execute();

    }

    @Override
    public void setMeta(InformationItem item, String key, String value) {
        setMeta(item, key, value, false);
    }

    @Override
    public void setMeta(InformationItem item, String key, String value, boolean isIndexed) {
        if (!(item instanceof CassandraInformationItemImpl))
            throw generateWrongDaoException();
        CassandraInformationItemImpl localItem = ((CassandraInformationItemImpl) item);

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

    @Override
    public void removeMeta(InformationItem item, String key) {
        if (!(item instanceof CassandraInformationItemImpl))
            throw generateWrongDaoException();
        CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;

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

    @Override
    public Collection<InformationItem> loadByMeta(String key, String value) {

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

    @Override
    public Map<UUID, String> searchByMetaPrefix(String key, String prefix) {

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

    /*
                             Private methods
     */

    private Collection<UUID> getUniqueIds(Collection<InformationItem> items) {
        Set<UUID> result = new HashSet<UUID>();
        for (InformationItem item : items) {
            CassandraInformationItemImpl local = (CassandraInformationItemImpl) item;
            result.add(local.uuid);
        }
        return result;
    }

    private UnsupportedOperationException generateWrongDaoException() {
        return new UnsupportedOperationException("This dao can't operate with this item");
    }

    private void addMetaIndex(CassandraInformationItemImpl item, String key, String value, Mutator<String> mutator, boolean isIndexed) {
        String rowKey = String.format(META_FORMAT, key, value);;
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

    private void removeMetaIndex(CassandraInformationItemImpl item, String key, String oldValue, Mutator<String> mutator) {
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
