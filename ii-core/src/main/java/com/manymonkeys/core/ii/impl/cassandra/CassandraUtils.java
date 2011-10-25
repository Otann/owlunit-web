package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.ColumnSlice;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;

import java.util.*;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class CassandraUtils {

    static final UUIDSerializer   us = UUIDSerializer.get();
    static final DoubleSerializer ds = DoubleSerializer.get();
    static final StringSerializer ss = StringSerializer.get();
    /**
     * Miltiget queries in Cassandra requires to set either query sizelimit or returnKeysOnly flag.
     * So when we need actual data from rows we have to determine limit of the query.
     * This affects querying metadata, components and parents
     */
    public static final int MULTIGET_COUNT = 10000;

    static List<UUID> getRow(Keyspace keyspace, String columnFamily, UUID key) {
        List<UUID> result = new LinkedList<UUID>();

        SliceQuery<UUID, UUID, Double> query = HFactory.createSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> slice = queryResult.get();

        for (HColumn<UUID, Double> column : slice.getColumns()) {
            result.add(column.getName());
        }

        return result;
    }

    static List<HColumn<UUID, Double>> getRowMap(Keyspace keyspace, String columnFamily, UUID key) {

        SliceQuery<UUID, UUID, Double> query = HFactory.createSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> slice = queryResult.get();

        return slice.getColumns();

    }

    static Collection<UUID> getIds(Collection<Ii> items) {
        List<UUID> result = new LinkedList<UUID>();
        for (Ii item : items) {
            CassandraIiImpl local = (CassandraIiImpl) item;
            result.add(local.uuid);
        }
        return result;
    }

    static Map<UUID, Ii> toMap(Collection<Ii> items) {
        Map<UUID, Ii> result = new HashMap<UUID, Ii>();
        if (items == null) {
            return result;
        }
        for (Ii item : items) {
            result.put(item.getUUID(), item);
        }
        return result;
    }

    static Rows<UUID, UUID, Double> multigetRows(Keyspace keyspace, String columnFamily, Collection<UUID> ids) {
        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(columnFamily);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();
        return queryResult.get();
    }
}
