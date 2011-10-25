package com.manymonkeys.core.ii.impl.cassandra;

import com.manymonkeys.core.ii.Ii;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.*;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import me.prettyprint.hector.api.query.SliceQuery;
import me.prettyprint.hector.api.query.SuperSliceQuery;

import java.util.*;

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

public class HectorUtils {

    static final UUIDSerializer   us = UUIDSerializer.get();
    static final DoubleSerializer ds = DoubleSerializer.get();
    static final StringSerializer ss = StringSerializer.get();

    /**
     * Multiget queries in Cassandra requires to set either query size limit or returnKeysOnly flag.
     * So when we need actual data from rows we have to determine limit of the query.
     * This affects querying metadata, components and parents
     */
    public static final int MULTIGET_COUNT = 10000;

    static List<UUID> getRowKeys(Keyspace keyspace, String columnFamily, UUID key) {
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

    static List<UUID> getSuperRowKeys(Keyspace keyspace, String columnFamily, UUID key) {
        List<UUID> result = new LinkedList<UUID>();

        SuperSliceQuery<UUID, UUID, UUID, Double> query = HFactory.createSuperSliceQuery(keyspace, us, us, us, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<SuperSlice<UUID, UUID, Double>> queryResult = query.execute();
        SuperSlice<UUID, UUID, Double> slice = queryResult.get();

        for (HSuperColumn<UUID, UUID, Double> column : slice.getSuperColumns()) {
            result.add(column.getName());
        }

        return result;
    }

    static List<HColumn<UUID, Double>> getColumns(Keyspace keyspace, String columnFamily, UUID key) {

        SliceQuery<UUID, UUID, Double> query = HFactory.createSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<UUID, Double>> queryResult = query.execute();
        ColumnSlice<UUID, Double> slice = queryResult.get();

        return slice.getColumns();
    }

    static Map<UUID, Double> getRow(Keyspace keyspace, String columnFamily, UUID key) {
        return columnsToMap(getColumns(keyspace, columnFamily, key));
    }

    static Map<UUID, Double> columnsToMap(Collection<HColumn<UUID, Double>> columns) {
        Map<UUID, Double> result = new HashMap<UUID, Double>();

        for (HColumn<UUID, Double> column : columns) {
            result.put(column.getName(), column.getValue());
        }

        return result;
    }

    static List<HSuperColumn<UUID, UUID, Double>> getSuperColumns(Keyspace keyspace, String columnFamily, UUID key) {

        SuperSliceQuery<UUID, UUID, UUID, Double> query = HFactory.createSuperSliceQuery(keyspace, us, us, us, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<SuperSlice<UUID, UUID, Double>> queryResult = query.execute();
        SuperSlice<UUID, UUID, Double> slice = queryResult.get();

        return slice.getSuperColumns();
    }

    static Map<UUID,Map<UUID, Double>> getSuperRow(Keyspace keyspace, String columnFamily, UUID key) {

        List<HSuperColumn<UUID, UUID, Double>> superColumns = getSuperColumns(keyspace, columnFamily, key);

        Map<UUID,Map<UUID, Double>> result = new HashMap<UUID, Map<UUID, Double>>();
        for(HSuperColumn<UUID, UUID, Double> sColumn : superColumns) {
            result.put(sColumn.getName(), columnsToMap(sColumn.getColumns()));
        }

        return result;
    }

    static Collection<UUID> getIds(Collection<Ii> items) {
        List<UUID> result = new LinkedList<UUID>();
        for (Ii item : items) {
            result.add(item.getUUID());
        }
        return result;
    }

    static Map<UUID,Map<UUID, Double>> multigetRows(Keyspace keyspace, String columnFamily, Collection<UUID> ids) {
        MultigetSliceQuery<UUID, UUID, Double> query = HFactory.createMultigetSliceQuery(keyspace, us, us, ds);
        query.setColumnFamily(columnFamily);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<UUID, UUID, Double>> queryResult = query.execute();

        Map<UUID,Map<UUID, Double>> result = new HashMap<UUID, Map<UUID, Double>>();
        for(Row<UUID, UUID, Double> row : queryResult.get()) {
            result.put(row.getKey(), columnsToMap(row.getColumnSlice().getColumns()));
        }

        return result;
    }
}
