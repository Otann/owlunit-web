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
     * Keyspace in Cassandra to store data
     */
    private Keyspace keyspace;

    // Column families constants
    private static final String CF_META = "META";
    private static final String CF_META_INDEX = "META_INDEX";
    private static final String CF_META_PREFIX = "META_PREFIX";
    private static final String CF_COMPONENTS = "COMPONENTS";
    private static final String CF_PARENTS = "PARENTS";

    // Currently not used
//    private static final String CF_SIMILAR = "SIMILAR";
//    private static final String CF_RECALCULATE = "RECALCULATE";

    public CassandraInformationItemDaoImpl(Keyspace keyspace) {
        this.keyspace = keyspace;
    }

    @Override
    public CassandraInformationItemImpl createInformationItem() {
        UUID uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
        return createInformationItem(uuid);
    }

    CassandraInformationItemImpl createInformationItem(UUID uuid) {
        return new CassandraInformationItemImpl(uuid, this);
    }

    @Override
    public void deleteInformationItem(InformationItem item) {
        if (item instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;

            Mutator<UUID> uuidMutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            for (InformationItem parent : item.getParents().keySet()) {
                uuidMutator.addDeletion(parent.getUUID(), CF_COMPONENTS, localItem.uuid, UUIDSerializer.get());
            }
            for (InformationItem component : item.getComponents().keySet()) {
                uuidMutator.addDeletion(component.getUUID(), CF_PARENTS, localItem.uuid, UUIDSerializer.get());
            }
            uuidMutator.addDeletion(localItem.uuid, CF_META, null, StringSerializer.get());
            uuidMutator.addDeletion(localItem.uuid, CF_COMPONENTS, null, StringSerializer.get());
            uuidMutator.addDeletion(localItem.uuid, CF_PARENTS, null, StringSerializer.get());
            uuidMutator.execute();

            Mutator<String> stringMutator = HFactory.createMutator(keyspace, StringSerializer.get());
            for (String key : localItem.meta.keySet()) {
                String oldValue = localItem.meta.get(key);
                String oldIndexKey = String.format(META_FORMAT, key, oldValue);
                stringMutator.addDeletion(oldIndexKey, CF_META_INDEX, localItem.uuid, UUIDSerializer.get());

                String[] oldWords = oldValue.toLowerCase().split("\\s");
                for (String word : oldWords) {
                    for (int i = 1; i <= word.length(); i++) {
                        String rowName = String.format(META_FORMAT, key, word.substring(0, i));
                        stringMutator.addDeletion(rowName, CF_META_PREFIX, localItem.uuid, UUIDSerializer.get());
                    }
                }
            }
            stringMutator.execute();
        }
    }

    public void reloadComponents(CassandraInformationItemImpl item) {
        if (item.components == null) {
            item.components = new HashMap<InformationItem, Double>();
        } else {
            item.components.clear();
        }

        UUIDSerializer us = UUIDSerializer.get();
        DoubleSerializer ds = DoubleSerializer.get();

        SliceQuery<UUID, UUID, Double> sliceQuery = HFactory.createSliceQuery(keyspace, us, us, ds);
        sliceQuery.setColumnFamily(CF_COMPONENTS);
        sliceQuery.setKey(item.uuid);
        sliceQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<UUID, Double>> queryResult = sliceQuery.execute();
        List<HColumn<UUID, Double>> columns = queryResult.get().getColumns();

        Map<UUID, Double> componentsMap = new HashMap<UUID, Double>();
        for (HColumn<UUID, Double> column : columns) {
            componentsMap.put(column.getName(), column.getValue());
        }

        Collection<InformationItem> components = multigetByUUID(componentsMap.keySet());
        for (InformationItem component : components) {
            item.components.put(component, componentsMap.get(component.getUUID()));
        }
    }

    @Override
    public Collection<InformationItem> multigetComponents(Collection<InformationItem> items) {

        Map<UUID, CassandraInformationItemImpl> itemsMap = collectionToUUIDMap(items);
        if (itemsMap.isEmpty()) {
            return Collections.emptySet();
        }

        UUIDSerializer us = UUIDSerializer.get();
        DoubleSerializer ds = DoubleSerializer.get();

        // Get list of ids of components
        MultigetSliceQuery<UUID, UUID, Double> componentsIdQuery = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        componentsIdQuery.setColumnFamily(CF_COMPONENTS);
        componentsIdQuery.setKeys(itemsMap.keySet());
        componentsIdQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = componentsIdQuery.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();

        // Prepare list for items query
        Set<UUID> componentsQuerySet = new HashSet<UUID>();
        for (Row<UUID, UUID, Double> row : rows) {
            for (HColumn<UUID, Double> column : row.getColumnSlice().getColumns()) {
                componentsQuerySet.add(column.getName());
            }
        }

        // Get all components with meta
        Collection<InformationItem> components = multigetByUUID(componentsQuerySet);
        if (components.isEmpty()) {
            return Collections.emptySet();
        }
        Map<UUID, CassandraInformationItemImpl> componentsMap = collectionToUUIDMap(components);

        // Attach components back to items
        for (Row<UUID, UUID, Double> row : rows) {
            CassandraInformationItemImpl item = itemsMap.get(row.getKey());

            if (item.components == null) {
                item.components = new HashMap<InformationItem, Double>();
            } else {
                item.components.clear();
            }

            for (HColumn<UUID, Double> column : row.getColumnSlice().getColumns()) {
                UUID link = column.getName();
                Double value = column.getValue();

                InformationItem component = componentsMap.get(link);
                item.components.put(component, value);
            }
        }

        return components;
    }

    public void reloadParents(CassandraInformationItemImpl item) {
        if (item.parents == null) {
            item.parents = new HashMap<InformationItem, Double>();
        } else {
            item.parents.clear();
        }

        UUIDSerializer us = UUIDSerializer.get();
        DoubleSerializer ds = DoubleSerializer.get();

        SliceQuery<UUID, UUID, Double> sliceQuery = HFactory.createSliceQuery(keyspace, us, us, ds);
        sliceQuery.setColumnFamily(CF_PARENTS);
        sliceQuery.setKey(item.uuid);
        sliceQuery.setRange(null, null, false, MULTIGET_COUNT);
        QueryResult<ColumnSlice<UUID, Double>> queryResult = sliceQuery.execute();
        List<HColumn<UUID, Double>> columns = queryResult.get().getColumns();

        Map<UUID, Double> parentsMap = new HashMap<UUID, Double>();
        for (HColumn<UUID, Double> column : columns) {
            parentsMap.put(column.getName(), column.getValue());
        }

        Collection<InformationItem> parents = multigetByUUID(parentsMap.keySet());
        for (InformationItem parent : parents) {
            item.parents.put(parent, parentsMap.get(parent.getUUID()));
        }
    }

    @Override
    public Collection<InformationItem> multigetParents(Collection<InformationItem> items) {

        Map<UUID, CassandraInformationItemImpl> itemsMap = collectionToUUIDMap(items);
        if (itemsMap.isEmpty()) {
            return Collections.emptySet();
        }

        UUIDSerializer us = UUIDSerializer.get();
        DoubleSerializer ds = DoubleSerializer.get();

        // Get list of ids of parents
        MultigetSliceQuery<UUID, UUID, Double> parentsIdQuery = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        parentsIdQuery.setColumnFamily(CF_PARENTS);
        parentsIdQuery.setKeys(itemsMap.keySet());
        parentsIdQuery.setRange(null, null, false, MULTIGET_COUNT);
        QueryResult<Rows<UUID, UUID, Double>> queryResult = parentsIdQuery.execute();
        Rows<UUID, UUID, Double> rows = queryResult.get();

        // Prepare list for items query
        Set<UUID> parentsQuerySet = new HashSet<UUID>();
        for (Row<UUID, UUID, Double> row : rows) {
            for (HColumn<UUID, Double> column : row.getColumnSlice().getColumns()) {
                parentsQuerySet.add(column.getName());
            }
        }

        // Get all parents with meta
        Collection<InformationItem> parents = multigetByUUID(parentsQuerySet);
        if (parents.isEmpty()) {
            return Collections.emptySet();
        }

        Map<UUID, CassandraInformationItemImpl> parentsMap = collectionToUUIDMap(parents);

        // Attach parents back to items
        for (Row<UUID, UUID, Double> row : rows) {
            CassandraInformationItemImpl item = itemsMap.get(row.getKey());

            if (item.parents == null) {
                item.parents = new HashMap<InformationItem, Double>();
            } else {
                item.parents.clear();
            }

            for (HColumn<UUID, Double> column : row.getColumnSlice().getColumns()) {
                UUID link = column.getName();
                Double value = column.getValue();

                InformationItem parent = parentsMap.get(link);
                item.parents.put(parent, value);
            }
        }

        return parents;
    }

    @Override
    public InformationItem getByUUID(UUID uuid) {
        UUIDSerializer us = UUIDSerializer.get();
        StringSerializer ss = StringSerializer.get();

        SliceQuery<UUID, String, String> sliceQuery = HFactory.createSliceQuery(keyspace, us, ss, ss);
        sliceQuery.setColumnFamily(CF_META);
        sliceQuery.setKey(uuid);
        sliceQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<String, String>> queryResult = sliceQuery.execute();
        ColumnSlice<String, String> results = queryResult.get();

        CassandraInformationItemImpl item = createInformationItem(uuid);
        for (HColumn<String, String> column : results.getColumns()) {
            item.meta.put(column.getName(), column.getValue());
        }

        return item;
    }

    @Override
    public Collection<InformationItem> multigetByUUID(Collection<UUID> uuids) {
        UUIDSerializer us = UUIDSerializer.get();
        StringSerializer ss = StringSerializer.get();

        MultigetSliceQuery<UUID, String, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
        multigetSliceQuery.setColumnFamily(CF_META);
        multigetSliceQuery.setKeys(uuids);
        multigetSliceQuery.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, String, String>> queryResult = multigetSliceQuery.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        List<InformationItem> result = new LinkedList<InformationItem>();
        for (Row<UUID, String, String> row : rows) {
            CassandraInformationItemImpl item = createInformationItem(row.getKey());
            for (HColumn<String, String> column : row.getColumnSlice().getColumns()) {
                item.meta.put(column.getName(), column.getValue());
            }
            result.add(item);
        }

        return result;
    }


    @Override
    public void setComponentWeight(InformationItem item, InformationItem component, Double weight) {
        if (item instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;
            CassandraInformationItemImpl localComponent = (CassandraInformationItemImpl) component;

            if (localItem.components != null) {
                localItem.components.put(component, weight);
            }
            if (localComponent.parents != null) {
                localComponent.parents.put(item, weight);
            }

            UUIDSerializer us = UUIDSerializer.get();
            DoubleSerializer ds = DoubleSerializer.get();

            Mutator<UUID> mutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            mutator.addInsertion(item.getUUID(), CF_COMPONENTS, HFactory.createColumn(component.getUUID(), weight, us, ds));
            mutator.addInsertion(component.getUUID(), CF_PARENTS, HFactory.createColumn(item.getUUID(), weight, us, ds));
            mutator.execute();

            //TODO: in case of precalculated recommendations add all parents of component to recalculate queue
        }
    }

    @Override
    public void removeComponent(InformationItem item, InformationItem component) {
        if (item instanceof CassandraInformationItemImpl && component instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;
            CassandraInformationItemImpl localComponent = (CassandraInformationItemImpl) component;

            if (localItem.components != null) {
                localItem.components.remove(component);
            }
            if (localComponent.parents != null) {
                localComponent.parents.remove(item);
            }

            Mutator<UUID> mutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            mutator.addDeletion(item.getUUID(), CF_COMPONENTS, component.getUUID(), UUIDSerializer.get());
            mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), UUIDSerializer.get());
            mutator.execute();

        }
    }

    @Override
    public void setMeta(InformationItem item, String key, String value) {
        if (item instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = ((CassandraInformationItemImpl) item);

            // Update data
            HFactory.createMutator(keyspace, UUIDSerializer.get())
                    .insert(item.getUUID(), CF_META, HFactory.createStringColumn(key, value));

            // Update index

            UUIDSerializer us = UUIDSerializer.get();
            DoubleSerializer ds = DoubleSerializer.get();
            StringSerializer ss = StringSerializer.get();
            Mutator<String> mutator = HFactory.createMutator(keyspace, ss);

            // If there was value, delete index for it
            if (localItem.meta.containsKey(key)) {
                String oldValue = localItem.meta.get(key);
                String oldIndexKey = String.format(META_FORMAT, key, oldValue);
                mutator.addDeletion(oldIndexKey, CF_META_INDEX, localItem.uuid, us);

                String[] oldWords = oldValue.toLowerCase().split("\\s");
                for (String word : oldWords) {
                    for (int i = 1; i <= word.length(); i++) {
                        String rowName = String.format(META_FORMAT, key, word.substring(0, i));
                        mutator.addDeletion(rowName, CF_META_PREFIX, localItem.uuid, us);
                    }
                }
            }

            // Add index for new value
            String[] words = value.toLowerCase().split("\\s");
            String indexKey = String.format(META_FORMAT, key, value);
            mutator.addInsertion(indexKey, CF_META_INDEX, HFactory.createColumn(item.getUUID(), 1D, us, ds));

            for (String word : words) {
                for (int i = 1; i <= word.length(); i++) {
                    String rowName = String.format(META_FORMAT, key, word.substring(0, i));
                    mutator.addInsertion(rowName, CF_META_PREFIX, HFactory.createColumn(item.getUUID(), value, us, ss));
                }
            }

            mutator.execute();

            // Update model
            localItem.meta.put(key, value);
        }
    }

    @Override
    public void removeMeta(InformationItem item, String key) {
        if (item instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;

            localItem.meta.remove(key);

            Mutator<UUID> uuidMutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            uuidMutator.addDeletion(item.getUUID(), CF_META, key, StringSerializer.get());
            uuidMutator.execute();

            Mutator<String> stringMutator = HFactory.createMutator(keyspace, StringSerializer.get());
            String oldValue = localItem.meta.get(key);
            String oldIndexKey = String.format(META_FORMAT, key, oldValue);
            stringMutator.addDeletion(oldIndexKey, CF_META_INDEX, localItem.uuid, UUIDSerializer.get());

            String[] oldWords = oldValue.toLowerCase().split("\\s");
            for (String word : oldWords) {
                for (int i = 1; i <= word.length(); i++) {
                    String rowName = String.format(META_FORMAT, key, word.substring(0, i));
                    stringMutator.addDeletion(rowName, CF_META_PREFIX, localItem.uuid, UUIDSerializer.get());
                }
            }
            stringMutator.execute();
        }
    }

    @Override
    public Collection<InformationItem> multigetByMeta(String key, String value) {

        UUIDSerializer us = UUIDSerializer.get();
        StringSerializer ss = StringSerializer.get();
        DoubleSerializer ds = DoubleSerializer.get();

        // in form of "key#value"
        String queryKey = String.format(META_FORMAT, key, value.toLowerCase());

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
        return multigetByUUID(result);
    }

    @Override
    public Map<UUID, String> searchByMetaPrefix(String key, String prefix) {
        UUIDSerializer us = UUIDSerializer.get();
        StringSerializer ss = StringSerializer.get();

        // in form of "key#value"
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

    private Map<UUID, CassandraInformationItemImpl> collectionToUUIDMap(Collection<InformationItem> items) {
        Map<UUID, CassandraInformationItemImpl> result = new HashMap<UUID, CassandraInformationItemImpl>();
        for (InformationItem item : items) {
            CassandraInformationItemImpl local = (CassandraInformationItemImpl) item;
            result.put(local.uuid, local);
        }
        return result;
    }

}
