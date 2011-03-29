package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;
import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.*;
import me.prettyprint.hector.api.exceptions.HInvalidRequestException;
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

    private Keyspace keyspace;

    // Column families constants
    private static final String CF_META = "META";
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

    @Override
    public void deleteInformationItem(InformationItem item) {
        if (item instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;

            Mutator<UUID> mutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            for (InformationItem parent : item.getParents().keySet()) {
                mutator.addDeletion(parent.getUUID(), CF_COMPONENTS, item.getUUID(), UUIDSerializer.get());
            }
            for (InformationItem component : item.getComponents().keySet()) {
                mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), UUIDSerializer.get());
            }
            mutator.addDeletion(item.getUUID(), CF_META, null, StringSerializer.get());
            mutator.execute();
        }
    }

    CassandraInformationItemImpl createInformationItem(UUID uuid) {
        return new CassandraInformationItemImpl(uuid, this);
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
            item.components.clear();
            for (HColumn<UUID, Double> column : row.getColumnSlice().getColumns()) {
                UUID link = column.getName();
                Double value = column.getValue();

                InformationItem component = componentsMap.get(link);
                item.components.put(component, value);
            }
        }

        return components;
    }

    //TODO: merge with previous method
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
            item.parents.clear();
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

        QueryResult<Rows<UUID, String, String>> queryResult = multigetSliceQuery.execute();
        Rows<UUID, String, String> rows = queryResult.get();

        List<InformationItem> result = new LinkedList<InformationItem>();
        for (Row<UUID, String, String> row : rows) {
            CassandraInformationItemImpl item = createInformationItem(row.getKey());
            for (HColumn<String, String> column : row.getColumnSlice().getColumns()) {
                item.meta.put(column.getName(), column.getValue());
            }
        }

        return result;
    }

    @Override
    public void setComponentWeight(InformationItem item, InformationItem component, Double weight) {
        if (item instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;
            CassandraInformationItemImpl localComponent = (CassandraInformationItemImpl) component;

            localItem.components.put(component, weight);
            localComponent.parents.put(item, weight);

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

            localItem.components.remove(component);
            localComponent.parents.remove(item);

            Mutator<UUID> mutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            mutator.addDeletion(item.getUUID(), CF_COMPONENTS, component.getUUID(), UUIDSerializer.get());
            mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), UUIDSerializer.get());
            mutator.execute();

        }
    }

    @Override
    public void setMeta(InformationItem item, String key, String value) {
        if (item instanceof CassandraInformationItemImpl) {
            Mutator<UUID> mutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            mutator.insert(item.getUUID(), CF_META, HFactory.createStringColumn(key, value));
        }
    }

    @Override
    public void removeMeta(InformationItem item, String key) {
        if (item instanceof CassandraInformationItemImpl) {
            CassandraInformationItemImpl localItem = (CassandraInformationItemImpl) item;

            localItem.meta.remove(key);

            Mutator<UUID> mutator = HFactory.createMutator(keyspace, UUIDSerializer.get());
            mutator.addDeletion(item.getUUID(), CF_META, key, StringSerializer.get());
            mutator.execute();
        }
    }

    @Override
    public Collection<InformationItem> multigetByMeta(String key, String value) {

        UUIDSerializer us = UUIDSerializer.get();
        StringSerializer ss = StringSerializer.get();

        IndexedSlicesQuery<UUID, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, us, ss, ss);
        query.setColumnFamily(CF_META);
        query.addEqualsExpression(key, value);
        query.setReturnKeysOnly();

        QueryResult<OrderedRows<UUID, String, String>> queryResult;
        try {
            queryResult = query.execute();
        } catch (HInvalidRequestException e) {
            return Collections.emptySet();
        }

        Rows<UUID, String, String> rows = queryResult.get();

        Collection<InformationItem> result = new LinkedList<InformationItem>();
        for (Row<UUID, String, String> row : rows) {
            CassandraInformationItemImpl item = createInformationItem(row.getKey());
            for (HColumn<String, String> column : row.getColumnSlice().getColumns()) {
                item.meta.put(column.getName(), column.getValue());
            }
            result.add(item);
        }

        return result;
    }

    public void loadComponents(CassandraInformationItemImpl item) {
        UUIDSerializer us = UUIDSerializer.get();
        DoubleSerializer ds = DoubleSerializer.get();

        SliceQuery<UUID, UUID, Double> sliceQuery = HFactory.createSliceQuery(keyspace, us, us, ds);
        sliceQuery.setColumnFamily(CF_COMPONENTS);
        sliceQuery.setKey(item.uuid);

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


    public void loadParents(CassandraInformationItemImpl item) {
        UUIDSerializer us = UUIDSerializer.get();
        DoubleSerializer ds = DoubleSerializer.get();

        SliceQuery<UUID, UUID, Double> sliceQuery = HFactory.createSliceQuery(keyspace, us, us, ds);
        sliceQuery.setColumnFamily(CF_PARENTS);
        sliceQuery.setKey(item.uuid);

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

    private Map<UUID, CassandraInformationItemImpl> collectionToUUIDMap(Collection<InformationItem> items) {
        Map<UUID, CassandraInformationItemImpl> result = new HashMap<UUID, CassandraInformationItemImpl>();
        for (InformationItem item : items) {
            CassandraInformationItemImpl local = (CassandraInformationItemImpl) item;
            result.put(local.uuid, local);
        }
        return result;
    }
}
