package com.manymonkeys.local;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.core.ii.InformationItemDao;
import com.manymonkeys.core.ii.impl.cassandra.CassandraInformationItemDaoImpl;
import com.manymonkeys.service.cinema.MovieService;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class CoreTest {

    static Logger logger = LoggerFactory.getLogger(CoreTest.class);

    public static void main(String[] args) {

        Cluster cluster = HFactory.getOrCreateCluster("LocalCluster", "localhost:9160");
        Keyspace keyspace = HFactory.createKeyspace("InformationItems", cluster);

        logger.info("=====================================");

        InformationItemDao dao = new CassandraInformationItemDaoImpl(keyspace);

        List<InformationItem> items = new LinkedList<InformationItem>();
        for (int i = 0; i < 10; i++) {
            InformationItem item = dao.createInformationItem();
            items.add(item);
            dao.setMeta(item, "key", "value" + i);
        }

        Collection<InformationItem> itemsByMultiget = dao.loadByMeta("key", "value1");
        Map<UUID, String> itemsBySearch = dao.searchByMetaPrefix("key", "value");

        logger.info(String.format("Items found by multiget: %s", itemsByMultiget));
        logger.info(String.format("Items found by prefix search: %s", itemsBySearch));

        for (InformationItem item : items) {
            dao.deleteInformationItem(item);
        }

        logger.info("=====================================");

        MovieService service = new MovieService(keyspace);

        service.createTag("new tag");
        InformationItem tag = service.getTag("new tag");
        logger.info("tag loaded");

        logger.info("=====================================");

        cluster.getConnectionManager().shutdown();

    }

}
