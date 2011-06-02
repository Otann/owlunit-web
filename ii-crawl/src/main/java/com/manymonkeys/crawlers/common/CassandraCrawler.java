package com.manymonkeys.crawlers.common;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import java.io.IOException;

/**
 * Rocket Science Software
 *
 * @author Ilya Pimenov
 */
public abstract class CassandraCrawler {

    public abstract void run(Keyspace keyspace) throws Exception;

    protected void crawl() {
        Cluster cluster = HFactory.getOrCreateCluster(
                PropertyManager.get(PropertyManager.Property.CASSANDRA_CLUSTER),
                PropertyManager.get(PropertyManager.Property.CASSANDRA_HOST));
        Keyspace keyspace = HFactory.createKeyspace(PropertyManager.get(PropertyManager.Property.CASSANDRA_KEYSPACE), cluster);

        try {
            run(keyspace);

            System.out.println("All done");
        } catch (Exception e) {
            System.out.println("Shit happened: " + e.getMessage());
        } finally {
            cluster.getConnectionManager().shutdown();
        }
    }

}
