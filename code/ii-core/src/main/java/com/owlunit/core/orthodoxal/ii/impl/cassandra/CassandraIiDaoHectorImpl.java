package com.owlunit.core.orthodoxal.ii.impl.cassandra;

import com.owlunit.core.orthodoxal.ii.Ii;
import com.owlunit.core.orthodoxal.ii.IiDao;
import com.owlunit.core.orthodoxal.ii.exception.DAOException;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.service.template.*;
import me.prettyprint.hector.api.beans.*;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.CounterQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.prettyprint.hector.api.*;

import java.util.*;

import static com.owlunit.core.orthodoxal.ii.impl.cassandra.HectorUtils.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CassandraIiDaoHectorImpl implements IiDao {

    @SuppressWarnings({"UnusedDeclaration"})
    final static Logger logger = LoggerFactory.getLogger(CassandraIiDaoHectorImpl.class);

    /**
     * Formats for keys in META_INDEX column family
     */
    private static final String META_INDEX_FORMAT = "%s#%s";

    /**
     * Each item has mark of it's creator
     * This is done because each item has to have at leas some meta information to be persisted
     * Otherwise we can not distinct non-existent item from item with no data
     */
    private static final String META_KEY_CREATOR = "CREATED BY";

    // Tech CFs
    static final String CF_COUNTERS   = "COUNTERS";
    static final String COUNTERS_KEY  = "ii";
    static final String COUNTERS_NAME = "ii";

    // Meta
    static final String CF_META       = "META";
    static final String CF_META_INDEX = "META_INDEX";
    // Tree direct links up
    static final String CF_PARENTS    = "PARENTS";
    static final String CF_OLDIES     = "OLDIES";
    // Tree direct links down
    static final String CF_COMPONENTS = "COMPONENTS";
    static final String CF_DIRECT_2 = "DIRECT_2";
    static final String CF_DIRECT_3 = "DIRECT_3";
    // Tree indirect links
    static final String CF_INDIRECT = "INDIRECT";

    ////////////////////////////////////////////////
    ////////////////    Column Families
    ////////////////////////////////////////////////

    private ColumnFamilyTemplate<Long, String> cfMeta;
    private ColumnFamilyTemplate<String, Long> cfMetaIndex;

    /**
     * Keyspace in Cassandra to store data
     */
    private Keyspace keyspace;

    public CassandraIiDaoHectorImpl(Keyspace keyspace) {
        this.keyspace = keyspace;

        cfMeta = new ThriftColumnFamilyTemplate<Long, String>(this.keyspace,
                CF_META,
                LongSerializer.get(),
                StringSerializer.get());

        cfMetaIndex = new ThriftColumnFamilyTemplate<String, Long>(this.keyspace,
                CF_META_INDEX,
                StringSerializer.get(),
                LongSerializer.get());

    }

    ////////////////////////////////////////////////
    ////////////////    Create / Delete / Load
    ////////////////////////////////////////////////

    @Override
    public CassandraIiImpl createInformationItem() {
        CassandraIiImpl item = new CassandraIiImpl(nextId());
        setMeta(item, META_KEY_CREATOR, this.getClass().getName());
        return item;
    }

    @Override
    public void deleteInformationItem(Ii ii) {
        CassandraIiImpl item = checkImpl(loadMeta(ii)); //query 1

        List<Long> invalidateItems = getRowKeys(keyspace, CF_PARENTS, item.id); //query 2

        Mutator<String> stringMutator = HFactory.createMutator(keyspace, ss);
        for(String key : item.meta.keySet()) {
            String value = item.meta.get(key);
            String rowKey = String.format(META_INDEX_FORMAT, key, value);
            stringMutator.addDeletion(rowKey, CF_META_INDEX);
        }
        stringMutator.execute();

        Mutator<Long> mutator = HFactory.createMutator(keyspace, ls);
        mutator.addDeletion(item.id, CF_META)
                .addDeletion(item.id, CF_COMPONENTS)
                .addDeletion(item.id, CF_PARENTS)
                .addDeletion(item.id, CF_OLDIES)
                .addDeletion(item.id, CF_DIRECT_2)
                .addDeletion(item.id, CF_DIRECT_3)
                .addDeletion(item.id, CF_INDIRECT)
                .execute();

        // Invalidate
        for (Long id : invalidateItems) { //TODO Anton Chebotaev - check
            updateDirectReferences(id, item.id, Priority.HIGH);
        }
    }

    @Override
    public Ii load(long id) {
        if (getMeta(id, META_KEY_CREATOR) == null) {
            return null;
        } else {
            return new CassandraIiImpl(id);
        }
    }

    @Override
    public Collection<Ii> load(Collection<Long> ids) {
        checkNull(ids);

        MultigetSliceQuery<Long, String, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keyspace, ls, ss, ss);
        multigetSliceQuery.setColumnFamily(CF_META);
        multigetSliceQuery.setKeys(ids);
        multigetSliceQuery.setRange(null, null, false, 1);

        QueryResult<Rows<Long, String, String>> queryResult = multigetSliceQuery.execute();
        Rows<Long, String, String> rows = queryResult.get();

        List<Ii> result = new ArrayList<Ii>();
        for (Row<Long, String, String> row : rows) {
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
        checkNull(key);
        CassandraIiImpl item = checkImpl(ii);

        // Update index
        Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
        String oldValue = getMeta(item.id, key);
        if (oldValue != null) {
            String rowKey = String.format(META_INDEX_FORMAT, key, oldValue);
            mutator.addDeletion(rowKey, CF_META_INDEX, item.id, ls);
        }
        if (isIndexed) {
            String rowKey = String.format(META_INDEX_FORMAT, key, value);
            mutator.addInsertion(rowKey, CF_META_INDEX, HFactory.createColumn(item.id, 1D, ls, ds));
        }
        mutator.execute();

        // Update data
        HFactory.createMutator(keyspace, ls)
                .insert(item.id, CF_META, HFactory.createStringColumn(key, value));

        // Update model
        if (notLoaded(item.meta)) {
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
        String oldValue = getMeta(item.id, key);
        if (oldValue != null) {
            String rowKey = String.format(META_INDEX_FORMAT, key, oldValue);
            cfMetaIndex.deleteColumn(rowKey, item.id);
        }

        // Data
        cfMeta.deleteColumn(item.id, key);

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
    public Collection<Ii> load(String key, String value) {
        checkNull(key);

        String queryKey = String.format(META_INDEX_FORMAT, key, value);
        ColumnFamilyResult<String, Long> query = cfMetaIndex.queryColumns(queryKey);
        return load(query.getColumnNames());
    }

    @Override
    public Ii loadMeta(Ii ii) {
        CassandraIiImpl item = checkImpl(ii);
        ColumnFamilyResult<Long, String> queryResult = cfMeta.queryColumns(item.id);
        return forceUpdateMetadata(item, queryResult);
    }

    @Override
    public Collection<Ii> loadMeta(Collection<Ii> items) {
        checkNull(items);

        if (items.isEmpty())
            return items;

        Collection<Ii> result = new ArrayList<Ii>();
        Map<Long, CassandraIiImpl> itemsMap = toMap(items);

        ColumnFamilyResult<Long, String> queryResults = cfMeta.queryColumns(itemsMap.keySet());
        while (queryResults.hasNext()) {
            ColumnFamilyResult<Long, String> queryResult = queryResults.next();
            result.add(forceUpdateMetadata(itemsMap.get(queryResult.getKey()), queryResult));
        }

        return result;
    }


    ////////////////////////////////////////////////
    ////////////////    Tree operations
    ////////////////////////////////////////////////

    @Override
    public Ii loadComponents(Ii item) {
        return forceUpdateComponents(item, getRow(keyspace, CF_COMPONENTS, item.getId()));
    }

    @Override
    public Collection<Ii> loadComponents(Collection<Ii> items) {
        checkNull(items);

        Collection<Ii> result = new ArrayList<Ii>();
        Map<Long,Map<Long, Double>> queryResult = multigetRows(keyspace, CF_COMPONENTS, getIds(items));
        for (Ii item : items) {
            if (queryResult.containsKey(item.getId())) {
                result.add(forceUpdateComponents(item, queryResult.get(item.getId())));
            }
        }
        return result;
    }

    @Override
    public Ii loadParents(Ii item) {
        return forceUpdateParents(item, getRow(keyspace, CF_PARENTS, item.getId()));
    }

    @Override
    public Collection<Ii> loadParents(Collection<Ii> items) {
        checkNull(items);

        Collection<Ii> result = new ArrayList<Ii>();
        Map<Long,Map<Long, Double>> queryResult = multigetRows(keyspace, CF_PARENTS, getIds(items));
        for (Ii item : items) {
            if (queryResult.containsKey(item.getId())) {
                result.add(forceUpdateParents(item, queryResult.get(item.getId())));
            }
        }
        return result;
    }

    @Override
    public Ii setComponentWeight(Ii itemIi, Ii componentIi, Double weight) {
        CassandraIiImpl item = checkImpl(itemIi);
        CassandraIiImpl component = checkImpl(componentIi);

        Mutator<Long> mutator = HFactory.createMutator(keyspace, ls);

        // Update tree level
        mutator.addInsertion(item.id, CF_COMPONENTS, HFactory.createColumn(component.id, weight, ls, ds));
        mutator.addInsertion(component.id, CF_PARENTS, HFactory.createColumn(item.id, weight, ls, ds));
        mutator.execute();

        updateDirectReferences(item.id, component.id, Priority.HIGH);

        // Updated object itself
        if (notLoaded(item.components)) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl(item);
            newItem.components.put(component, weight);
            return newItem;
        }
    }

    @Override
    public Ii removeComponent(Ii itemIi, Ii componentIi) {
        CassandraIiImpl item = checkImpl(itemIi);
        CassandraIiImpl component = checkImpl(componentIi);

        // Update component level
        Mutator<Long> mutator = HFactory.createMutator(keyspace, ls);
        mutator.addDeletion(item.getId(), CF_COMPONENTS, component.getId(), ls);
        mutator.addDeletion(component.getId(), CF_PARENTS, item.getId(), ls);
        mutator.execute();

        updateDirectReferences(item.id, component.id, Priority.HIGH);

        // Updated object itself
        if (notLoaded(item.components)) {
            return item;
        } else {
            CassandraIiImpl newItem = new CassandraIiImpl(item);
            newItem.components.remove(component);
            return newItem;
        }
    }

    @Override
    public Map<Ii, Double> getIndirectComponents(Ii ii) {
        CassandraIiImpl item = checkImpl(ii);
        Map<Ii, Double> result = new HashMap<Ii, Double>();

        Map<Long, Double> queryResult = getRow(keyspace, CF_INDIRECT, item.id);
        for(Map.Entry<Long, Double> entry : queryResult.entrySet()) {
            result.put(new CassandraIiImpl(entry.getKey()), entry.getValue());
        }

        return result;
    }

    ////////////////////////////////////////////////
    ////////////////    Asynchronous calls
    ////////////////////////////////////////////////

    private static enum Priority { HIGH, MEDIUM, LOW }

    //TODO Ilya Pimenov - apply JMS here
    private void updateDirectReferences(long item, long component, @SuppressWarnings("UnusedParameters") Priority priority) {

        Mutator<Long> mutator = HFactory.createMutator(keyspace, ls);

        // Updates for item

        List<HColumn<Long, Double>> componentChildren = getColumns(keyspace, CF_COMPONENTS, component); //query 1
        HSuperColumn<Long, Long, Double> componentSuperColumn = HFactory.createSuperColumn(component, componentChildren, ls, ls, ds);


        // Update CF_DIRECT_2
        mutator.addInsertion(item, CF_DIRECT_2, componentSuperColumn);

        // Update CF_DIRECT_3
        List<Long> itemDirect2Keys = getSuperRowKeys(keyspace, CF_DIRECT_2, item); //query 2
        List<HSuperColumn<Long, Long, Double>> componentDirect2 = getSuperColumns(keyspace, CF_DIRECT_2, component); //query 3
        for(HSuperColumn<Long, Long, Double> superColumn : componentDirect2) {
            if (!itemDirect2Keys.contains(superColumn.getName())) {
                mutator.addInsertion(item, CF_DIRECT_3, superColumn);
            }
        }

        // updates for parents

        List<HColumn<Long, Double>> itemChildren = getColumns(keyspace, CF_COMPONENTS, item); //query 4
        HSuperColumn<Long, Long, Double> itemSuperColumn = HFactory.createSuperColumn(item, itemChildren, ls, ls, ds);

        // Update DIRECT_2
        List<Long> itemParents = getRowKeys(keyspace, CF_PARENTS, item); //query 5
        for(Long parent : itemParents) {
            mutator.addInsertion(parent, CF_DIRECT_2, itemSuperColumn);
            mutator.addInsertion(component, CF_OLDIES, HFactory.createColumn(parent, 0D, ls, ds));
        }

        // Update DIRECT_3
        List<Long> itemOldies = getRowKeys(keyspace, CF_OLDIES, item); //query 6
        for(Long oldie : itemOldies) {
            mutator.addInsertion(oldie, CF_DIRECT_3, itemSuperColumn);
        }

        mutator.execute();

        updateIndirectReferences(item, Priority.HIGH);
        for(Long parent : itemParents) {
            updateIndirectReferences(parent, Priority.MEDIUM);
        }
        for(Long oldie : itemOldies) {
            updateIndirectReferences(oldie, Priority.LOW);
        }
    }

    //TODO Ilya Pimenov - apply JMS here
    private void updateIndirectReferences(Long item, @SuppressWarnings("UnusedParameters") Priority priority) {
        Map<Long, Double> components = getRow(keyspace, CF_COMPONENTS, item);            //query 1
        Map<Long, Map<Long, Double>> direct2 = getSuperRow(keyspace, CF_DIRECT_2, item); //query 2
        Map<Long, Map<Long, Double>> direct3 = getSuperRow(keyspace, CF_DIRECT_3, item); //query 3

        Map<Long, Double> indirect = new HashMap<Long, Double>(components);

        for(Long componentId : components.keySet()) {
            Double componentWeight = components.get(componentId);
            increaseMapEntry(indirect, componentId, componentWeight);

            if (direct2.get(componentId) != null) {
                for (Long direct2id : direct2.get(componentId).keySet()) {

                    Double direct2weight = direct2.get(componentId).get(direct2id);
                    increaseMapEntry(indirect, direct2id, (componentWeight + direct2weight) * 0.25);

                    if (direct3.get(direct2id) != null) {
                        for (Long direct3id : direct3.get(direct2id).keySet()) {

                            Double direct3weight = direct3.get(direct2id).get(direct3id);
                            increaseMapEntry(indirect, direct3id, (componentWeight + direct2weight + direct3weight) * 0.125);
                        }
                    }
                }
            }
        }

        Mutator<Long> mutator = HFactory.createMutator(keyspace, ls);
        for (Long indirectId : indirect.keySet()) {
            mutator.addInsertion(item, CF_INDIRECT, HFactory.createColumn(indirectId, indirect.get(indirectId), ls, ds));
        }

        mutator.execute();
    }

    private static void increaseMapEntry(Map<Long, Double> map, long key, Double value) {
        if (map.containsKey(key)) {
            Double oldValue = map.get(key);
            map.put(key, oldValue + value);
        } else {
            map.put(key, value);
        }
    }

    private long nextId() {

        Mutator<String> mutator = HFactory.createMutator(keyspace, ss);
        mutator.incrementCounter(COUNTERS_KEY, CF_COUNTERS, COUNTERS_NAME, 1);
        mutator.execute();

        CounterQuery<String, String> query = HFactory.createCounterColumnQuery(keyspace, ss, ss)
                .setColumnFamily(CF_COUNTERS)
                .setKey(COUNTERS_KEY)
                .setName(COUNTERS_NAME);
        QueryResult<HCounterColumn<String>> queryResult = query.execute();

        Long id = queryResult.get().getValue();
        if (id == null) {
            throw new DAOException("Can not load counter for Ii");
        } else {
            return id;
        }

    }

    ////////////////////////////////////////////////
    ////////////////    Deprecated
    ////////////////////////////////////////////////

    @Override
    @Deprecated
    public Collection<Ii> search(String key, String prefix) {
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////
    ////////////////    Private
    ////////////////////////////////////////////////

    private static void checkNull(String s) {
        if (s == null) {
            throw new IllegalArgumentException("String can not be null");
        }
    }

    private static void checkNull(Collection items) {
        if (items == null) {
            throw new IllegalArgumentException();
        }
        for (Object o : items) {
            if (o == null) {
                throw new IllegalArgumentException("Each item should not be null");
            }
        }
    }

    private static CassandraIiImpl checkImpl(Ii item) {
        if (item instanceof CassandraIiImpl) {
            return (CassandraIiImpl) item;
        } else {
            throw new IllegalArgumentException("This dao can't operate with this item");
        }
    }
    
    private static boolean notLoaded(Map c) {
        return CassandraIiImpl.NOT_LOADED.equals(c);
    }

    static Map<Long, CassandraIiImpl> toMap(Collection<Ii> items) {
        Map<Long, CassandraIiImpl> result = new HashMap<Long, CassandraIiImpl>();
        if (items == null) {
            return result;
        }
        for (Ii ii : items) {
            CassandraIiImpl item = checkImpl(ii);
            result.put(item.getId(), item);
        }
        return result;
    }

    private String getMeta(long id, String key) {
        HColumn<String, String> queryResult = cfMeta.querySingleColumn(id, key, String.class);

        if (queryResult == null) {
            return null;
        } else {
            return queryResult.getValue();
        }
    }

    private static Ii forceUpdateMetadata(CassandraIiImpl item, ColumnFamilyResult<Long, String> queryResult) {
        CassandraIiImpl newItem = new CassandraIiImpl(item);
        newItem.meta = new HashMap<String, String>();

        for (String key : queryResult.getColumnNames()) {
            newItem.meta.put(key, queryResult.getString(key));
        }

        return newItem;
    }

    private static Ii forceUpdateComponents(Ii itemIi, Map<Long, Double> data) {
        CassandraIiImpl item = checkImpl(itemIi);

        CassandraIiImpl newItem;
        if (item.components == null) {
            newItem = item;
        } else {
            newItem = new CassandraIiImpl(item);
        }

        newItem.components = new HashMap<Ii, Double>();
        Map<Long, CassandraIiImpl> oldComponents = toMap(item.components.keySet());

        for (Map.Entry<Long, Double> entry : data.entrySet()) {
            Long componentId = entry.getKey();
            Double weight = entry.getValue();

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

    private static Ii forceUpdateParents(Ii itemIi, Map<Long, Double> data) {
        CassandraIiImpl item = checkImpl(itemIi);

        CassandraIiImpl newItem;
        if (item.components == null) {
            newItem = item;
        } else {
            newItem = new CassandraIiImpl(item);
        }

        newItem.parents = new HashMap<Ii, Double>();
        Map<Long, CassandraIiImpl> oldParents = toMap(item.parents.keySet());

        for (Map.Entry<Long, Double> entry : data.entrySet()) {
            Long parentId = entry.getKey();
            Double weight = entry.getValue();

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
