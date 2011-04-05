package com.manymonkeys.crawlers.movielens;

import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Many Monkeys
 *
 * @author Ilya Pimenov
 */
public class PropertyManager {

    public static enum Property {
        NEO4J_DB("neo4j.db"),

        CASSANDRA_CLUSTER("cassandra.cluster"),
        CASSANDRA_HOST("cassandra.address"),
        CASSANDRA_KEYSPACE("cassandra.keyspace"),

        MOVIES_DATA_FILE("movies.data.file"),
        TAGS_DATA_FILE("tags.data.file");

        String value;

        Property(String value) {
            this.value = value;
        }
    }

    private static final Properties properties = new Properties();
    private static final String PROPERTIES_BUNDLE_NAME = "crawl";

    static {
        ResourceBundle resources = ResourceBundle.getBundle(PROPERTIES_BUNDLE_NAME, Locale.getDefault());
        for (String key : resources.keySet()) {
            properties.put(key, resources.getString(key));
        }
    }

    public static String get(final Property key) {
        Object value = properties.get(key.value);
        return value != null ? value.toString() : null;
    }

    public static void put(Property key, Object value) {
        properties.put(key.value, value);
    }
}