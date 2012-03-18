package com.owlunit.core.orthodoxal.ii.impl.cassandra;

import com.owlunit.core.orthodoxal.ii.Ii;
import me.prettyprint.cassandra.serializers.DoubleSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
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

    static final LongSerializer ls = LongSerializer.get();
    static final DoubleSerializer ds = DoubleSerializer.get();
    static final StringSerializer ss = StringSerializer.get();

    /**
     * Multiget queries in Cassandra requires to set either query size limit or returnKeysOnly flag.
     * So when we need actual data from rows we have to determine limit of the query.
     * This affects querying metadata, components and parents
     */
    public static final int MULTIGET_COUNT = 10000;

    static List<Long> getRowKeys(Keyspace keyspace, String columnFamily, Long key) {
        List<Long> result = new LinkedList<Long>();

        SliceQuery<Long, Long, Double> query = HFactory.createSliceQuery(keyspace, ls, ls, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<Long, Double>> queryResult = query.execute();
        ColumnSlice<Long, Double> slice = queryResult.get();

        for (HColumn<Long, Double> column : slice.getColumns()) {
            result.add(column.getName());
        }

        return result;
    }

    static List<Long> getSuperRowKeys(Keyspace keyspace, String columnFamily, Long key) {
        List<Long> result = new LinkedList<Long>();

        SuperSliceQuery<Long, Long, Long, Double> query = HFactory.createSuperSliceQuery(keyspace, ls, ls, ls, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<SuperSlice<Long, Long, Double>> queryResult = query.execute();
        SuperSlice<Long, Long, Double> slice = queryResult.get();

        for (HSuperColumn<Long, Long, Double> column : slice.getSuperColumns()) {
            result.add(column.getName());
        }

        return result;
    }

    static List<HColumn<Long, Double>> getColumns(Keyspace keyspace, String columnFamily, Long key) {

        SliceQuery<Long, Long, Double> query = HFactory.createSliceQuery(keyspace, ls, ls, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<ColumnSlice<Long, Double>> queryResult = query.execute();
        ColumnSlice<Long, Double> slice = queryResult.get();

        return slice.getColumns();
    }

    static Map<Long, Double> getRow(Keyspace keyspace, String columnFamily, Long key) {
        return columnsToMap(getColumns(keyspace, columnFamily, key));
    }

    static Map<Long, Double> columnsToMap(Collection<HColumn<Long, Double>> columns) {
        Map<Long, Double> result = new HashMap<Long, Double>();

        for (HColumn<Long, Double> column : columns) {
            result.put(column.getName(), column.getValue());
        }

        return result;
    }

    static List<HSuperColumn<Long, Long, Double>> getSuperColumns(Keyspace keyspace, String columnFamily, Long key) {

        SuperSliceQuery<Long, Long, Long, Double> query = HFactory.createSuperSliceQuery(keyspace, ls, ls, ls, ds);
        query.setColumnFamily(columnFamily);
        query.setKey(key);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<SuperSlice<Long, Long, Double>> queryResult = query.execute();
        SuperSlice<Long, Long, Double> slice = queryResult.get();

        return slice.getSuperColumns();
    }

    static Map<Long,Map<Long, Double>> getSuperRow(Keyspace keyspace, String columnFamily, Long key) {

        List<HSuperColumn<Long, Long, Double>> superColumns = getSuperColumns(keyspace, columnFamily, key);

        Map<Long,Map<Long, Double>> result = new HashMap<Long, Map<Long, Double>>();
        for(HSuperColumn<Long, Long, Double> sColumn : superColumns) {
            result.put(sColumn.getName(), columnsToMap(sColumn.getColumns()));
        }

        return result;
    }

    static Collection<Long> getIds(Collection<Ii> items) {
        List<Long> result = new LinkedList<Long>();
        for (Ii item : items) {
            result.add(item.getId());
        }
        return result;
    }

    static Map<Long,Map<Long, Double>> multigetRows(Keyspace keyspace, String columnFamily, Collection<Long> ids) {
        MultigetSliceQuery<Long, Long, Double> query = HFactory.createMultigetSliceQuery(keyspace, ls, ls, ds);
        query.setColumnFamily(columnFamily);
        query.setKeys(ids);
        query.setRange(null, null, false, MULTIGET_COUNT);

        QueryResult<Rows<Long, Long, Double>> queryResult = query.execute();

        Map<Long,Map<Long, Double>> result = new HashMap<Long, Map<Long, Double>>();
        for(Row<Long, Long, Double> row : queryResult.get()) {
            result.put(row.getKey(), columnsToMap(row.getColumnSlice().getColumns()));
        }

        return result;
    }
}
