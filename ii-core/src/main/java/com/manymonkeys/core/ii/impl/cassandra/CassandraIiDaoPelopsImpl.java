//package com.manymonkeys.core.ii.impl.cassandra;
//
//import com.manymonkeys.core.ii.Ii;
//import com.manymonkeys.core.ii.IiDao;
//import org.apache.cassandra.thrift.ConsistencyLevel;
//import org.safehaus.uuid.UUIDGenerator;
//import org.scale7.cassandra.pelops.Cluster;
//import org.scale7.cassandra.pelops.Mutator;
//import org.scale7.cassandra.pelops.Pelops;
//import org.scale7.cassandra.pelops.Selector;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Collection;
//import java.util.Map;
//import java.util.UUID;
//
///**
// * @author Anton Chebotaev
// *         Owls Proprietary
// */
//
//public class CassandraIiDaoPelopsImpl implements IiDao {
//
//    @SuppressWarnings({"UnusedDeclaration"})
//    final static Logger logger = LoggerFactory.getLogger(CassandraIiDaoHectorImpl.class);
//
//    /**
//     * Formats for keys in META_INDEX column family
//     */
//    private static final String META_INDEX_FORMAT = "%s#%s";
//
//    /**
//     * Each item has mark of it's creator
//     * This is done because each item has to have at leas some meta information to be persisted
//     * Otherwise we can not distinct non-existent item from item with no data
//     */
//    private static final String META_KEY_CREATOR = "CREATED BY";
//
//    ////////////////////////////////////////////////
//    ////////////////    Cassandra
//    ////////////////////////////////////////////////
//
//    private static final String KEYSPACE      = "INFORMATION_ITEMS";
//
//    // Meta
//    private static final String CF_META       = "META";
//    private static final String CF_META_INDEX = "META_INDEX";
//
//    // Tree direct links up
//    private static final String CF_PARENTS    = "PARENTS";
//    private static final String CF_OLDIES     = "OLDIES";
//
//    // Tree direct links down
//    private static final String CF_COMPONENTS = "COMPONENTS";
//    private static final String CF_DIRECT_2 = "DIRECT-2";
//    private static final String CF_DIRECT_3 = "DIRECT-3";
//
//    // Tree indirect links
//    private static final String CF_INDIRECT = "INDIRECT";
//
//    ////////////////////////////////////////////////
//    ////////////////    Pelops
//    ////////////////////////////////////////////////
//
//    private static final String POOL = "pool"; //TODO: change name
//
//    public CassandraIiDaoPelopsImpl(Cluster cluster) {
//        Pelops.addPool(POOL, cluster, KEYSPACE);
//    }
//
//    ////////////////////////////////////////////////
//    ////////////////    Create / Delete / Load
//    ////////////////////////////////////////////////
//
//    @Override
//    public Ii createInformationItem() {
//        UUID uuid = UUID.fromString(new com.eaio.uuid.UUID().toString());
//        CassandraIiImpl item = new CassandraIiImpl(uuid);
//        setMeta(item, META_KEY_CREATOR, this.getClass().getName());
//        return item;
//    }
//
//    @Override
//    public void deleteInformationItem(Ii item) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Ii load(UUID uuid) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Collection<Ii> load(Collection<UUID> uuids) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    ////////////////////////////////////////////////
//    ////////////////    Meta
//    ////////////////////////////////////////////////
//
//    @Override
//    public Ii setMeta(Ii item, String key, String value) {
//        return setMetaExtended(item, key, value, true);
//    }
//
//    @Override
//    public Ii setMetaUnindexed(Ii item, String key, String value) {
//        return setMetaExtended(item, key, value, false);
//    }
//
//    private Ii setMetaExtended(Ii ii, String key, String value, boolean indexed) {
//        CassandraIiImpl item = checkImpl(ii);
//
//        Mutator mutator = createMutator();
//
//    }
//
//    @Override
//    public Collection<Ii> load(String key, String value) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Ii loadMeta(Ii item) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Collection<Ii> loadMeta(Collection<Ii> items) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Ii removeMeta(Ii item, String key) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    ////////////////////////////////////////////////
//    ////////////////    Tree operations
//    ////////////////////////////////////////////////
//
//    @Override
//    public Ii loadComponents(Ii item) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Collection<Ii> loadComponents(Collection<Ii> items) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Ii loadParents(Ii item) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Collection<Ii> loadParents(Collection<Ii> items) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Map<UUID, String> search(String key, String prefix) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Ii setComponentWeight(Ii item, Ii component, Double weight) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Ii removeComponent(Ii item, Ii component) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public Map<Ii, Double> getIndirectComponents(Ii item) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    ////////////////////////////////////////////////
//    ////////////////    Private
//    ////////////////////////////////////////////////
//
//    private static Mutator createMutator() {
//        return Pelops.createMutator(POOL);
//    }
//
//    private static Selector createSelector() {
//        return Pelops.createSelector(POOL);
//    }
//
//    private static CassandraIiImpl checkImpl(Ii item) {
//        if (item instanceof CassandraIiImpl) {
//            return (CassandraIiImpl) item;
//        } else {
//            throw new UnsupportedOperationException("This dao can't operate with this item");
//        }
//    }
//
//    private String getMeta(UUID id, String key) {
//        Selector s = createSelector();
//        s.getColumnFromRow(CF_META, id, key, false, ConsistencyLevel.ONE);
//
//    }
//
//}
