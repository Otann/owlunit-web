package com.manymonkeys.local;

import me.prettyprint.cassandra.model.IndexedSlicesQuery;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.beans.Row;
import me.prettyprint.hector.api.beans.Rows;
import me.prettyprint.hector.api.exceptions.HectorException;
import me.prettyprint.hector.api.factory.HFactory;
import me.prettyprint.hector.api.mutation.Mutator;
import me.prettyprint.hector.api.query.ColumnQuery;
import me.prettyprint.hector.api.query.MultigetSliceQuery;
import me.prettyprint.hector.api.query.QueryResult;
import org.safehaus.uuid.UUIDGenerator;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class HectorTest {

    public static void main(String args[]) {
        Cluster cluster = HFactory.getOrCreateCluster("LocalCluster", "localhost:9160");
        Keyspace keyspace = HFactory.createKeyspace("InformationItems", cluster);

        UUIDSerializer us = UUIDSerializer.get();
        StringSerializer ss = StringSerializer.get();

//        HFactory.createKeyspaceDefinition()
//        KeyspaceDefinition ksDef = cluster.describeKeyspace("InformationItems");
//        for(ColumnFamilyDefinition cfDef : ksDef.getCfDefs()) {
//            if (cfDef.getName().equals("META")) {
//                BasicColumnDefinition columnDefinition = new BasicColumnDefinition();
//                columnDefinition.setName(ss.toByteBuffer("first"));
//                columnDefinition.setIndexType(ColumnIndexType.KEYS);
//                cfDef.getColumnMetadata().add(columnDefinition);
//            }
//        }
//        cluster.updateKeyspace(ksDef);
//
//        HFactory.createStringColumn("first", "John")


        try {
            UUID uuid = UUID.fromString(UUIDGenerator.getInstance().generateTimeBasedUUID().toString());
            Mutator<UUID> mutator = HFactory.createMutator(keyspace, us);
            mutator.insert(uuid, "META", HFactory.createStringColumn("first", "John"));
            mutator.insert(uuid, "META", HFactory.createStringColumn("second", "Mark"));
            mutator.insert(uuid, "META", HFactory.createStringColumn("created", (new Date()).toString()));

            ColumnQuery<UUID, String, String> columnQuery = HFactory.createColumnQuery(keyspace, us, ss, ss);
            columnQuery.setColumnFamily("META").setKey(uuid).setName("first");
            QueryResult<HColumn<String, String>> result = columnQuery.execute();

            System.out.println("Read HColumn from cassandra: " + result.get());
            System.out.println(String.format("Verify on CLI with:  get META['%s']; ", uuid.toString()));

            IndexedSlicesQuery<UUID, String, String> query = HFactory.createIndexedSlicesQuery(keyspace, us, ss, ss);
            query.setColumnFamily("META");
            query.addEqualsExpression("first", "John");
            query.setReturnKeysOnly();
            Rows<UUID, String, String> queryRows = query.execute().get();
            Collection<UUID> keys = new LinkedList<UUID>();
            for (Row<UUID, String, String> row : queryRows) {
                keys.add(row.getKey());
            }
            System.out.println("\nFound with IndexedSlicesQuery: " + keys.toString());

            MultigetSliceQuery<UUID, String, String> multigetQuery = HFactory.createMultigetSliceQuery(keyspace, us, ss, ss);
            multigetQuery.setColumnFamily("META");
            multigetQuery.setKeys(keys);
            multigetQuery.setRange(null, null, false, 0);
            Rows<UUID, String, String> multigetResult = multigetQuery.execute().get();
            System.out.println("\nFound with multigetSlicesQuery: ");
            for (Row<UUID, String, String> row : multigetResult) {
                System.out.println(String.format("Key: %s with columns: %s", row.getKey(), row.getColumnSlice()));
            }

        } catch (HectorException e) {
            e.printStackTrace();
        }
        cluster.getConnectionManager().shutdown();
    }
}