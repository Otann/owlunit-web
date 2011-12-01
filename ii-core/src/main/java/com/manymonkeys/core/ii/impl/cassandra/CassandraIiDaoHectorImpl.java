package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.core.ii.IiDao;
import com.manymonkeys.security.shiro.annotation.OwledArgument;
import com.manymonkeys.security.shiro.annotation.OwledMethod;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.cassandra.service.template.*;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.HSuperColumn;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import org.safehaus.uuid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.prettyprint.hector.api.*;

import java.util.*;

import static com.manymonkeys.core.ii.impl.cassandra.HectorUtils.*;

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

    private ColumnFamilyTemplate<UUID, String> cfMeta;
    private ColumnFamilyTemplate<String, UUID> cfMetaIndex;

    /**
     * Keyspace in Cassandra to store data
     */
    private Keyspace keyspace;

    public CassandraIiDaoHectorImpl(Keyspace keyspace) {
        this.keyspace = keyspace;

        cfMeta = new ThriftColumnFamilyTemplate<UUID, String>(this.keyspace,
                CF_META,
                UUIDSerializer.get(),
                StringSerializer.get());

        cfMetaIndex = new ThriftColumnFamilyTemplate<String, UUID>(this.keyspace,
                CF_META_INDEX,
                StringSerializer.get(),
                UUIDSerializer.get());

    }

    ////////////////////////////////////////////////
    ////////////////    Create / Delete / Load
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
        CassandraIiImpl item = checkImpl(loadMeta(ii)); //query 1

        List<UUID> invalidateItems = getRowKeys(keyspace, CF_PARENTS, item.id); //query 2

        Mutator<String> stringMutator = HFactory.createMutator(keyspace, HectorUtils.ss);
        for(String key : item.meta.keySet()) {
            String value = item.meta.get(key);
            String rowKey = String.format(META_INDEX_FORMAT, key, value);
            stringMutator.addDeletion(rowKey, CF_META_INDEX);
        }
        stringMutator.execute();

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, HectorUtils.us);
        mutator.addDeletion(item.id, CF_META)
                .addDeletion(item.id, CF_COMPONENTS)
                .addDeletion(item.id, CF_PARENTS)
                .addDeletion(item.id, CF_OLDIES)
                .addDeletion(item.id, CF_DIRECT_2)
                .addDeletion(item.id, CF_DIRECT_3)
                .addDeletion(item.id, CF_INDIRECT)
                .execute();

        // Invalidate
        for (UUID id : invalidateItems) { //TODO Anton Chebotaev - check
            updateDirectReferences(id, item.id, Priority.HIGH);
        }
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

        List<Ii> result = new LinkedList<Ii>();
        ColumnFamilyResult<UUID, String> query = cfMeta.queryColumns(uuids);
        while (query.hasNext()) {
            result.add(new CassandraIiImpl(query.next().getKey()));
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
        Mutator<String> mutator = HFactory.createMutator(keyspace, HectorUtils.ss);
        String oldValue = getMeta(item.id, key);
        if (oldValue != null) {
            String rowKey = String.format(META_INDEX_FORMAT, key, oldValue);
            mutator.addDeletion(rowKey, CF_META_INDEX, item.id, HectorUtils.us);
        }
        if (isIndexed) {
            String rowKey = String.format(META_INDEX_FORMAT, key, value);
            mutator.addInsertion(rowKey, CF_META_INDEX, HFactory.createColumn(item.id, 1D, HectorUtils.us, HectorUtils.ds));
        }
        mutator.execute();

        // Update data
        HFactory.createMutator(keyspace, HectorUtils.us)
                .insert(item.id, CF_META, HFactory.createStringColumn(key, value));

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
        String queryKey = String.format(META_INDEX_FORMAT, key, value);
        ColumnFamilyResult<String, UUID> query = cfMetaIndex.queryColumns(queryKey);
        return load(query.getColumnNames());
    }

    @Override
    public Ii loadMeta(Ii ii) {
        CassandraIiImpl item = checkImpl(ii);
        ColumnFamilyResult<UUID, String> queryResult = cfMeta.queryColumns(item.id);
        return forceUpdateMetadata(item, queryResult);
    }

    @Override
    public Collection<Ii> loadMeta(Collection<Ii> items) {
        if (items.isEmpty())
            return items;

        Collection<Ii> result = new LinkedList<Ii>();
        Map<UUID, CassandraIiImpl> itemsMap = toMap(items);

        ColumnFamilyResult<UUID, String> queryResults = cfMeta.queryColumns(itemsMap.keySet());
        while (queryResults.hasNext()) {
            ColumnFamilyResult<UUID, String> queryResult = queryResults.next();
            result.add(forceUpdateMetadata(itemsMap.get(queryResult.getKey()), queryResult));
        }

        return result;
    }


    ////////////////////////////////////////////////
    ////////////////    Tree operations
    ////////////////////////////////////////////////

    @Override
    public Ii loadComponents(Ii item) {
        return forceUpdateComponents(item, getRow(keyspace, CF_COMPONENTS, item.getUUID()));
    }

    @Override
    public Collection<Ii> loadComponents(Collection<Ii> items) {
        Collection<Ii> result = new LinkedList<Ii>();
        Map<UUID,Map<UUID, Double>> queryResult = multigetRows(keyspace, CF_COMPONENTS, getIds(items));
        for (Ii item : items) {
            if (queryResult.containsKey(item.getUUID())) {
                result.add(forceUpdateComponents(item, queryResult.get(item.getUUID())));
            }
        }
        return result;
    }

    @Override
    public Ii loadParents(Ii item) {
        return forceUpdateParents(item, getRow(keyspace, CF_PARENTS, item.getUUID()));
    }

    @OwledMethod
    @Override
    public Collection<Ii> loadParents(@OwledArgument Collection<Ii> items) {
        Collection<Ii> result = new LinkedList<Ii>();
        Map<UUID,Map<UUID, Double>> queryResult = multigetRows(keyspace, CF_PARENTS, getIds(items));
        for (Ii item : items) {
            if (queryResult.containsKey(item.getUUID())) {
                result.add(forceUpdateParents(item, queryResult.get(item.getUUID())));
            }
        }
        return result;
    }

    @Override
    public Ii setComponentWeight(Ii itemIi, Ii componentIi, Double weight) {
        CassandraIiImpl item = checkImpl(itemIi);
        CassandraIiImpl component = checkImpl(componentIi);

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, HectorUtils.us);

        // Update tree level
        mutator.addInsertion(item.id, CF_COMPONENTS, HFactory.createColumn(component.id, weight, HectorUtils.us, HectorUtils.ds));
        mutator.addInsertion(component.id, CF_PARENTS, HFactory.createColumn(item.id, weight, HectorUtils.us, HectorUtils.ds));
        mutator.execute();

        updateDirectReferences(item.id, component.id, Priority.HIGH);

        // Updated object itself
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
        CassandraIiImpl item = checkImpl(itemIi);
        CassandraIiImpl component = checkImpl(componentIi);

        // Update component level
        Mutator<UUID> mutator = HFactory.createMutator(keyspace, HectorUtils.us);
        mutator.addDeletion(item.getUUID(), CF_COMPONENTS, component.getUUID(), HectorUtils.us);
        mutator.addDeletion(component.getUUID(), CF_PARENTS, item.getUUID(), HectorUtils.us);
        mutator.execute();

        updateDirectReferences(item.id, component.id, Priority.HIGH);

        // Updated object itself
        if (item.getComponents() == null) {
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

        Map<UUID, Double> queryResult = getRow(keyspace, CF_INDIRECT, item.id);
        for(Map.Entry<UUID, Double> entry : queryResult.entrySet()) {
            result.put(new CassandraIiImpl(entry.getKey()), entry.getValue());
        }

        return result;
    }

    ////////////////////////////////////////////////
    ////////////////    Asynchronous calls
    ////////////////////////////////////////////////

    private static enum Priority { HIGH, MEDIUM, LOW }

    //TODO Ilya Pimenov - apply JMS here
    private void updateDirectReferences(UUID item, UUID component, Priority priority) {

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, HectorUtils.us);

        // Updates for item

        List<HColumn<UUID, Double>> componentChildren = getColumns(keyspace, CF_COMPONENTS, component); //query 1
        HSuperColumn<UUID, UUID, Double> componentSuperColumn = HFactory.createSuperColumn(component, componentChildren, us, us, ds);

        // Update CF_DIRECT_2
        mutator.addInsertion(item, CF_DIRECT_2, componentSuperColumn);

        // Update CF_DIRECT_3
        List<UUID> itemDirect2Keys = getSuperRowKeys(keyspace, CF_DIRECT_2, item); //query 2
        List<HSuperColumn<UUID, UUID, Double>> componentDirect2 = getSuperColumns(keyspace, CF_DIRECT_2, component); //query 3
        for(HSuperColumn<UUID, UUID, Double> superColumn : componentDirect2) {
            if (!itemDirect2Keys.contains(superColumn.getName())) {
                mutator.addInsertion(item, CF_DIRECT_3, superColumn);
            }
        }

        // updates for parents

        List<HColumn<UUID, Double>> itemChildren = getColumns(keyspace, CF_COMPONENTS, item); //query 4
        HSuperColumn<UUID, UUID, Double> itemSuperColumn = HFactory.createSuperColumn(item, itemChildren, us, us, ds);

        // Update DIRECT_2
        List<UUID> itemParents = getRowKeys(keyspace, CF_PARENTS, item); //query 5
        for(UUID parent : itemParents) {
            mutator.addInsertion(parent, CF_DIRECT_2, itemSuperColumn);
            mutator.addInsertion(component, CF_OLDIES, HFactory.createColumn(parent, 0D, HectorUtils.us, HectorUtils.ds));
        }

        // Update DIRECT_3
        List<UUID> itemOldies = getRowKeys(keyspace, CF_OLDIES, item); //query 6
        for(UUID oldie : itemOldies) {
            mutator.addInsertion(oldie, CF_DIRECT_3, itemSuperColumn);
        }

        mutator.execute();

        updateIndirectReferences(item, Priority.HIGH);
        for(UUID parent : itemParents) {
            updateIndirectReferences(parent, Priority.MEDIUM);
        }
        for(UUID oldie : itemOldies) {
            updateIndirectReferences(oldie, Priority.LOW);
        }
    }

    //TODO Ilya Pimenov - apply JMS here
    private void updateIndirectReferences(UUID item, Priority priority) {
        Map<UUID, Double> components = getRow(keyspace, CF_COMPONENTS, item); //query 1
        Map<UUID, Map<UUID, Double>> direct2 = getSuperRow(keyspace, CF_DIRECT_2, item); //query 2
        Map<UUID, Map<UUID, Double>> direct3 = getSuperRow(keyspace, CF_DIRECT_3, item); //query 3

        Map<UUID, Double> indirect = new HashMap<UUID, Double>(components);

        for(UUID componentId : components.keySet()) {
            Double componentWeight = components.get(componentId);
            incrementMapEntry(indirect, componentId, componentWeight);
            for (UUID direct2id : direct2.get(componentId).keySet()) {
                Double direct2weight = direct2.get(componentId).get(direct2id);
                incrementMapEntry(indirect, direct2id, (componentWeight + direct2weight) * 0.25);
                for (UUID direct3id : direct3.get(direct2id).keySet()) {
                    Double direct3weight = direct3.get(direct2id).get(direct3id);
                    incrementMapEntry(indirect, direct3id, (componentWeight + direct2weight + direct3weight) * 0.125);
                }
            }
        }

        Mutator<UUID> mutator = HFactory.createMutator(keyspace, HectorUtils.us);
        for (UUID indirectId : indirect.keySet()) {
            mutator.addInsertion(item, CF_INDIRECT, HFactory.createColumn(indirectId, indirect.get(indirectId), HectorUtils.us, HectorUtils.ds));
        }

        mutator.execute();
    }

    private static void  incrementMapEntry(Map<UUID, Double> map, UUID key, Double value) {
        if (map.containsKey(key)) {
            Double oldValue = map.get(key);
            map.put(key, oldValue + value);
        } else {
            map.put(key, value);
        }
    }

    ////////////////////////////////////////////////
    ////////////////    Deprecated
    ////////////////////////////////////////////////

    @Override
    public Map<UUID, String> search(String key, String prefix) {
        throw new UnsupportedOperationException();
    }

    ////////////////////////////////////////////////
    ////////////////    Private
    ////////////////////////////////////////////////

    private static CassandraIiImpl checkImpl(Ii item) {
        if (item instanceof CassandraIiImpl) {
            return (CassandraIiImpl) item;
        } else {
            throw new UnsupportedOperationException("This dao can't operate with this item");
        }
    }

    static Map<UUID, CassandraIiImpl> toMap(Collection<Ii> items) {
        Map<UUID, CassandraIiImpl> result = new HashMap<UUID, CassandraIiImpl>();
        if (items == null) {
            return result;
        }
        for (Ii ii : items) {
            CassandraIiImpl item = checkImpl(ii);
            result.put(item.getUUID(), item);
        }
        return result;
    }

    private String getMeta(UUID id, String key) {
        HColumn<String, String> queryResult = cfMeta.querySingleColumn(id, key, String.class);

        if (queryResult == null) {
            return null;
        } else {
            return queryResult.getValue();
        }
    }

    private static Ii forceUpdateMetadata(CassandraIiImpl item, ColumnFamilyResult<UUID, String> queryResult) {
        CassandraIiImpl newItem = new CassandraIiImpl(item);
        newItem.meta = new HashMap<String, String>();

        for (String key : queryResult.getColumnNames()) {
            newItem.meta.put(key, queryResult.getString(key));
        }

        return newItem;
    }

    private static Ii forceUpdateComponents(Ii itemIi, Map<UUID, Double> data) {
        CassandraIiImpl item = checkImpl(itemIi);

        CassandraIiImpl newItem;
        if (item.components == null) {
            newItem = item;
        } else {
            newItem = new CassandraIiImpl(item);
        }

        newItem.components = new HashMap<Ii, Double>();
        Map<UUID, CassandraIiImpl> oldComponents = toMap(item.components.keySet());

        for (Map.Entry<UUID, Double> entry : data.entrySet()) {
            UUID componentId = entry.getKey();
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

    private static Ii forceUpdateParents(Ii itemIi, Map<UUID, Double> data) {
        CassandraIiImpl item = checkImpl(itemIi);

        CassandraIiImpl newItem;
        if (item.components == null) {
            newItem = item;
        } else {
            newItem = new CassandraIiImpl(item);
        }

        newItem.parents = new HashMap<Ii, Double>();
        Map<UUID, CassandraIiImpl> oldParents = toMap(item.parents.keySet());

        for (Map.Entry<UUID, Double> entry : data.entrySet()) {
            UUID parentId = entry.getKey();
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
