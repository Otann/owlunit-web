package com.manymonkeys.crawlers.movielens;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.crawlers.common.PropertyManager;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many Monkeys
 *
 * @author Ilya Pimenov
 */
public class MovieLensTagsParser {

    public final double INITIAL_WEIGHT = Double.parseDouble(PropertyManager.get(PropertyManager.Property.MOVIELENS_TAG_WEIGHT_INITIAL));
    public final double ADDITIONAL_WEIGHT = Double.parseDouble(PropertyManager.get(PropertyManager.Property.MOVIELENS_TAG_WEIGHT_ADDITIONAL));

    public static void main(String[] args) throws IOException {
        new MovieLensTagsParser().run(args[0]);
    }

    public void run(String filePath) throws IOException {

        Cluster cluster = HFactory.getOrCreateCluster(
                PropertyManager.get(PropertyManager.Property.CASSANDRA_CLUSTER),
                PropertyManager.get(PropertyManager.Property.CASSANDRA_HOST));
        Keyspace keyspace = HFactory.createKeyspace(PropertyManager.get(PropertyManager.Property.CASSANDRA_KEYSPACE), cluster);

        try {

            MovieService movieService = new MovieService(keyspace);

            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));

            Map<String, InformationItem> tagCache = new HashMap<String, InformationItem>();
            Map<String, InformationItem> moviesCache = new HashMap<String, InformationItem>();

            TimeWatch watch = TimeWatch.start();

            String line = fileReader.readLine();
            Pattern p = Pattern.compile("\\:\\:(.*)\\:\\:");
            while (line != null) {
                if ("".equals(line))
                    continue;

                try {
                    Matcher matcher = p.matcher(line);
                    matcher.find();
                    String str = matcher.group(1);
                    String externalId = str.substring(0, str.indexOf(':'));
                    String tagName = str.substring(str.lastIndexOf(':') + 1, str.length()).toLowerCase();

                    InformationItem movieItem = moviesCache.get(externalId);
                    if (movieItem == null) {
                        movieItem = movieService.loadByMeta(MovieLensMoviesParser.EXTERNAL_ID, externalId).iterator().next();
                        moviesCache.put(externalId, movieItem);
                    }

                    watch.tick(50, "Processing movielens tags", "tags");

                    InformationItem tagItem = tagCache.get(tagName);
                    if (tagItem == null) {
                        tagItem = movieService.createTag(tagName);
                        tagCache.put(tagName, tagItem);
                    }

                    Double weight = movieItem.getComponentWeight(tagItem);
                    if (weight == null) {
                        movieService.setComponentWeight(movieItem, tagItem, INITIAL_WEIGHT);
                    } else {
                        movieService.setComponentWeight(movieItem, tagItem, weight + ADDITIONAL_WEIGHT);
                    }

                } catch (Exception e) {
                    System.out.printf("Failed to parse line %s; reason: %s%n", line, e.getMessage());
                    e.printStackTrace();
                } finally {
                    line = fileReader.readLine();
                }

            }

            System.out.println("All done");
        } finally {
            cluster.getConnectionManager().shutdown();
        }
    }

}
