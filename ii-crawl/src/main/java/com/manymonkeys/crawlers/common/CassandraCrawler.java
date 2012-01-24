package com.manymonkeys.crawlers.common;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * Rocket Science Software
 *
 * @author Ilya Pimenov
 */
public abstract class CassandraCrawler {

    protected ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");

    public abstract void run() throws Exception;

    protected void crawl() {
        try {
            run();
            ctx.close();
            System.out.println("All done");
        } catch (Exception e) {
            System.out.println("Shit happened: " + e.getMessage());
        }

    }

}
