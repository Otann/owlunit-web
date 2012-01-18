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

    public abstract void run() throws Exception;

    protected void crawl() {
        try {
            run();

            System.out.println("All done");
        } catch (Exception e) {
            System.out.println("Shit happened: " + e.getMessage());
        }

    }

}
