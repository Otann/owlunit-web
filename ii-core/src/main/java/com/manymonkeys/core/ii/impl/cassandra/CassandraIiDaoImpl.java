package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.security.shiro.annotation.OwledArgument;
import com.manymonkeys.security.shiro.annotation.OwledMethod;
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

    // Tree
    private static final String CF_META = "META";
    private static final String CF_META_INDEX = "META_INDEX";
    private static final String CF_COMPONENTS = "COMPONENTS";
    private static final String CF_PARENTS = "PARENTS";

    // Adapted weights
    private static final String CF_DIRECT_2 = "DIRECT-2";
    private static final String CF_DIRECT_3 = "DIRECT-3";
    private static final String CF_INDIRECT = "INDIRECT";

    // Typed cache
    private static final String CF_CACHE_TYPE_MOVIE = "CACHE-TYPE-MOVIE";


    /**
     * Keyspace in Cassandra to store data
     */
    private Keyspace keyspace;

    public CassandraIiDaoImpl(Keyspace keyspace) {
        this.keyspace = keyspace;
    }

    ////////////////////////////////////////////////
    ////////////////    Create / Delete
    ////////////////////////////////////////////////

    @Override
    public CassandraIiImpl createInformationItem() {
        UUID uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        CassandraIiImpl item = new CassandraIiImpl(uuid);
        setMeta(item, META_KEY_CREATOR, this.getClass().getName());
        return item;
    }

    @Override
    public void deleteInformationItem(Ii ii) {
        CassandraIiImpl item = checkImpl(ii);

        Mutator<UUID> uuidMutator = HFactory.createMutator(keyspace, CassandraUtils.us);

        // Removing item's rows
        uuidMutator.addDeletion(item.uuid, CF_META, null, CassandraUtils.ss)
                   .addDeletion(item.uuid, CF_COMPONENTS, null, CassandraUtils.ss)
                   .addDeletion(item.uuid, CF_PARENTS, null, CassandraUtils.ss);

        List<UUID> parents = CassandraUtils.getRow(keyspace, CF_PARENTS, item.uuid);
        List<UUID> components = CassandraUtils.getRow(keyspace, CF_COMPONENTS, item.uuid);

        // Removing links from CF_PARENTS
        for (UUID componentId : components) {
            uuidMutator.addDeletion(componentId, CF_PARENTS, item.uuid, CassandraUtils.us);
        }

        // Removing links from CF_COMPONENTS
        for (UUID parentId : parents) {
            uuidMutator.addDeletion(parentId, CF_COMPONENTS, item.uuid, CassandraUtils.us);
            uuidMutator.addDeletion(parentId, CF_INDIRECT, item.uuid, CassandraUtils.us);
        }

        // Removing links from CF_DIRECT_2
        Set<UUID> grandParents = new HashSet<UUID>();
        Set<UUID> oldies = new HashSet<UUID>();

        for(UUID parent : parents) {
            grandParents.addAll(CassandraUtils.getRow(keyspace, CF_PARENTS, parent));
        }
        for(UUID grandParent : grandParents) {
            uuidMutator.addDeletion(grandParent, CF_DIRECT_2, item.uuid, CassandraUtils.us);
            uuidMutator.addDeletion(grandParent, CF_INDIRECT, item.uuid, CassandraUtils.us);
            oldies.addAll(CassandraUtils.getRow(keyspace, CF_PARENTS, grandParent));
        }

        // Removing links from CF_DIRECT_3
        for(UUID oldie : oldies) {
            uuidMutator.addDeletion(oldie, CF_DIRECT_3, item.uuid, CassandraUtils.us);
            uuidMutator.addDeletion(oldie, CF_INDIRECT, item.uuid, CassandraUtils.us);
        }

        //TODO: remove from any recommendation cache (or not?)

        uuidMutator.execute();

        // Remove meta and index
        Mutator<String> stringMutator = HFactory.createMutator(keyspace, CassandraUtils.ss);
        for (String key : loadMetadata(item).getMetaMap().keySet()) {
            String oldValue = getMeta(item.getUUID(), key);
            String rowKey = String.format(META_FORMAT, key, oldValue);
            stringMutator.addDeletion(rowKey, CF_META_INDEX, item.getUUID(), CassandraUtils.us);
        }
        stringMutator.execute();
    }

    @OwledMethod
    @Override
    public Ii load(UUID uuid) {
        if (getMeta(uuid, META_KEY_CREATOR) == null) {
            return null;
        } else {
            return new CassandraIiImpl(uuid);
        }
    }

    @OwledMethod
    @Override
    public Collection<Ii> load(@OwledArgument Collection<UUID> uuids) {

        MultigetSliceQuery<UUID, String, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keyspace, CassandraUtils.us, CassandraUtils.ss, CassandraUtils.ss);
        multigetSliceQuery.setColumnFamily(CF_META);
        multigetSliceQuery.setKeys(uuids);
        multigetSliceQuery.setRange(null, null, false, 1);

        QueryResult<Rows<UUID, String, String>> queryResult = multigetSliceQuery.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        List<Ii> result = new LinkedList<Ii>();
        for (Row<UUID, String, String> row : rows) {
            result.add(new CassandraIiImpl(row.getKey()));
        }

        return result;
    }

    ////////////////////////////////////////////////
    ////////////////    Meta
    ////////////////////////////////////////////////

    @Override
    public Ii setMeta(Ii item, String key, String value) {
        return setMetaExtended(item, key, value, true);
    }

    @Override
    public Ii setMetaUnindexed(Ii item, String key, String value) {
        return setMetaExtended(item, key, value, false);
    }

    private Ii setMetaExtended(Ii ii, String key, String value, boolean isIndexed) {
        CassandraIiImpl item = checkImpl(ii);

        // Update index
        String oldValue = getMeta(item.uuid, key);
        Mutator<String> mutator = HFactory.createMutator(keyspace, CassandraUtils.ss);
        if (oldValue != null) {
            String rowKey = String.format(META_FORMAT, key, oldValue);
            mutator.addDeletion(rowKey, CF_META_INDEX, item.uuid, CassandraUtils.us);
        }
        if (isIndexed) {
            String rowKey = String.format(META_FORMAT, key, value);
            mutator.addInsertion(rowKey, CF_META_INDEX, HFactory.createColumn(item.uuid, 1D, CassandraUtils.us, CassandraUtils.ds));
        }
        mutator.execute();

        // Update data
        HFactory.createMutator(keyspace, CassandraUtils.us).insert(item.uuid, CF_META, HFactory.createStringColumn(key, value));

        // Update model
        if (item.meta == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl(item);
            newItem.meta = new HashMap<String, String>(item.meta);
            newItem.meta.put(key, value);
            return item;
        }
    }

    @Override
    public Ii removeMeta(Ii ii, String key) {
        CassandraIiImpl item = checkImpl(ii);

        // Index
        String oldValue = getMeta(item.uuid, key);
        Mutator<String> stringMutator = HFactory.createMutator(keyspace, CassandraUtils.ss);

        String rowKey = String.format(META_FORMAT, key, oldValue);
        stringMutator.addDeletion(rowKey, CF_META_INDEX, item.getUUID(), CassandraUtils.us);
        stringMutator.execute();

        // Data
        HFactory.createMutator(keyspace, CassandraUtils.us).delete(item.getUUID(), CF_META, key, CassandraUtils.ss);

        // Update model
        if (item.meta == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl(item);
            newItem.meta = new HashMap<String, String>(item.meta);
            newItem.meta.remove(key);
            return item;
        }
    }

    @Override
    public Ii loadMetadata(Ii ii) {
        CassandraIiImpl item = checkImpl(ii);

        SliceQuery<UUID, String, String> query = HFactory.createSliceQuery(keyspace, CassandraUtils.us, CassandraUtils.ss, CassandraUtils.ss);
        query.setColumnFamily(CF_META);
        query.setKey(item.uuid);
        query.setRange(null, null, false, CassandraUtils.MULTIGET_COUNT);

        QueryResult<ColumnSlice<String, String>> queryResult = query.execute();
        ColumnSlice<String, String> slice = queryResult.get();

        return forceUpdateMetadata(item, slice.getColumns());
    }

    @Override
    public Collection<Ii> loadMetadata(Collection<Ii> items) {
        if (items.isEmpty())
            return items;

        Collection<Ii> result = new LinkedList<Ii>();

        MultigetSliceQuery<UUID, String, String> query = HFactory.createMultigetSliceQuery(keyspace, CassandraUtils.us, CassandraUtils.ss, CassandraUtils.ss);
        query.setColumnFamily(CF_META);
        query.setKeys(CassandraUtils.getIds(items));
        query.setRange(null, null, false, CassandraUtils.MULTIGET_COUNT);

        QueryResult<Rows<UUID, String, String>> queryResult = query.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        for (Ii ii : items) {
            CassandraIiImpl item = checkImpl(ii);

            result.add(forceUpdateMetadata(item, rows.getByKey(item.uuid).getColumnSlice().getColumns()));
        }
        return result;
    }

    @Override
    public Collection<Ii> load(String key, String value) {

        String queryKey = String.format(META_FORMAT, key, value);

        SliceQuery<String, UUID, Double> query = HFactory.createSliceQuery(keyspace, CassandraUtils.ss, CassandraUtils.us, CassandraUtils.ds)
                .setColumnFamily(CF_META_INDEX)
                .setKey(queryKey)
                .setRange(null, null, false, CassandraUtils.MULTIGET_COUNT);

        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> columns = queryResult.get();

        Collection<UUID> uuids = new LinkedList<UUID>();
        for (HColumn<UUID, Double> column : columns.getColumns()) {
            uuids.add(column.getName());
        }

        return load(uuids);
    }

    ////////////////////////////////////////////////
    ////////////////    Components
    ////////////////////////////////////////////////

    @Override
    public Ii loadComponents(Ii item) {
        List<HColumn<UUID, Double>> row = CassandraUtils.getRowMap(keyspace, CF_COMPONENTS, item.getUUID());
        return forceUpdateComponents(item, row);

    }

    @Override
    public Collection<Ii> loadComponents(Collection<Ii> items) {
        Collection<Ii> result = new LinkedList<Ii>();
        Rows<UUID, UUID, Double> rows = CassandraUtils.multigetRows(keyspace, CF_COMPONENTS, CassandraUtils.getIds(items));
        for (Ii item : items) {
            result.add(forceUpdateComponents(item, rows.getByKey(item.getUUID()).getColumnSlice().getColumns()));
        }
        return result;
    }

    @Override
    public Ii loadParents(Ii item) {
        List<HColumn<UUID, Double>> row = CassandraUtils.getRowMap(keyspace, CF_PARENTS, item.getUUID());
        return forceUpdateParents(item, row);
    }

    @OwledMethod
    @Override
    public Collection<Ii> loadParents(@OwledArgument Collection<Ii> items) {
        Collection<Ii> result = new LinkedList<Ii>();
        Rows<UUID, UUID, Double> rows = CassandraUtils.multigetRows(keyspace, CF_PARENTS, CassandraUtils.getIds(items));
        for (Ii item : items) {
            result.add(forceUpdateParents(item, rows.getByKey(item.getUUID()).getColumnSlice().getColumns()));
        }
        return result;
    }

    @Override
    public Ii setComponentWeight(Ii itemIi, Ii componentIi, Double weight) {

        //TODO Anton Chebotaev - update for new CF

        CassandraIiImpl item = checkImpl(itemIi);
        CassandraIiImpl component = checkImpl(componentIi);

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, CassandraUtils.us);
        mutator.addInsertion(item.uuid, CF_COMPONENTS, HFactory.createColumn(component.uuid, weight, CassandraUtils.us, CassandraUtils.ds));
        mutator.addInsertion(component.uuid, CF_PARENTS, HFactory.createColumn(item.uuid, weight, CassandraUtils.us, CassandraUtils.ds));
        mutator.execute();

        if (item.getComponents() == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl(item);
            newItem.components.put(component, weight);
            return newItem;
        }
    }

    @Override
    public Ii removeComponent(Ii itemIi, Ii componentIi) {

        //TODO Anton Chebotaev - update for new CF

        CassandraIiImpl item = checkImpl(itemIi);
        CassandraIiImpl component = checkImpl(componentIi);

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, CassandraUtils.us);
        mutator.addDeletion(item.getUUID(), CF_COMPONENTS, component.getUUID(), CassandraUtils.us);
        mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), CassandraUtils.us);
        mutator.execute();

        if (item.getComponents() == null) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl((CassandraIiImpl) item);
            newItem.components.remove(component);
            return newItem;
        }
    }

    @Override
    public Map<UUID, String> search(String key, String prefix) {
        throw new UnsupportedOperationException();
    }

    /*------------------\
    |   P R I V A T E   |
    \__________________*/

    private static CassandraIiImpl checkImpl(Ii item) {
        if (item instanceof CassandraIiImpl) {
            return (CassandraIiImpl) item;
        } else {
            throw new UnsupportedOperationException("This dao can't operate with this item");
        }
    }

    private String getMeta(UUID id, String key) {
        ColumnQuery<UUID, String, String> query = HFactory.createColumnQuery(keyspace, CassandraUtils.us, CassandraUtils.ss, CassandraUtils.ss);
        query.setColumnFamily(CF_META);
        query.setKey(id);
        query.setName(key);

        QueryResult<HColumn<String, String>> queryResult = query.execute();
        if (queryResult.get() == null) {
            return null;
        } else {
            return queryResult.get().getValue();
        }
    }

    private static Ii forceUpdateMetadata(CassandraIiImpl item, Collection<HColumn<String, String>> columns) {
        CassandraIiImpl newItem = new CassandraIiImpl(item);
        newItem.meta = new HashMap<String, String>();

        for (HColumn<String, String> column : columns) {
            newItem.meta.put(column.getName(), column.getValue());
        }

        return newItem;
    }

    private static Ii forceUpdateComponents(Ii itemIi, Collection<HColumn<UUID, Double>> columns) {
        CassandraIiImpl item = checkImpl(itemIi);

        CassandraIiImpl newItem;
        if (item.components == null) {
            newItem = item;
        } else {
            newItem = new CassandraIiImpl(item);
        }

        newItem.components = new HashMap<Ii, Double>();
        Map<UUID, Ii> oldComponents = CassandraUtils.toMap(item.components.keySet());

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

    private static Ii forceUpdateParents(Ii itemIi, Collection<HColumn<UUID, Double>> columns) {
        CassandraIiImpl item = checkImpl(itemIi);

        CassandraIiImpl newItem;
        if (item.components == null) {
            newItem = item;
        } else {
            newItem = new CassandraIiImpl(item);
        }

        newItem.parents = new HashMap<Ii, Double>();
        Map<UUID, Ii> oldParents = CassandraUtils.toMap(item.parents.keySet());

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

}
