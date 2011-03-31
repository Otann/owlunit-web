package com.manymonkeys.crawlers.movielens;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many Monkeys
 *
 * @author Ilya Pimenov
 */
public class MovieLensTagsParser {

    private static long DEFAULT_WEIGHT = 1;

    public static void main(String[] args) throws IOException {
        Cluster cluster = HFactory.getOrCreateCluster(
                PropertyManager.get(PropertyManager.Property.CASSANDRA_CLUSTER),
                PropertyManager.get(PropertyManager.Property.CASSANDRA_HOST));
        Keyspace keyspace = HFactory.createKeyspace(PropertyManager.get(PropertyManager.Property.CASSANDRA_CLUSTER), cluster);

        try {

            MovieService movieService = new MovieService(keyspace);
            TagService tagService = new TagService(keyspace);

            String filePath = PropertyManager.get(PropertyManager.Property.TAGS_DATA_FILE);

            BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

            String line = fileReader.readLine();
            long done = 0;
            Pattern p = Pattern.compile("\\:\\:(.*)\\:\\:");
            while (line != null) {


                if ("".equals(line)) {
                    continue;
                }

                try {
                    Matcher m = p.matcher(line);
                    m.find();
                    String str = m.group(1);
                    String externalId = str.substring(0, str.indexOf(':'));
                    String tagName = str.substring(str.lastIndexOf(':') + 1, str.length());

                    InformationItem movie;
                    try {
                        movie = movieService.multigetByMeta(MovieLensMoviesParser.EXTERNAL_ID, externalId).iterator().next();
                    } catch (Exception e) {
                        System.out.println("Failed to find movie with external Id = " + externalId +
                                "; " + e.getMessage());
                        continue;
                    }

                    done++;
                    if (done % 25 == 0) {
                        System.out.println(String.format("Created %d tags", done));
                    }

                    InformationItem tag = tagService.getTag("\"" + tagName + "\"");
                    double weight = 0;
                    if (tag == null) {
                        tag = movieService.createTag(tagName);
                        weight = DEFAULT_WEIGHT;
                    } else if (movie.getComponentWeight(tag) > 0) {
                        weight = movie.getComponentWeight(tag) + 1;
                    } else {
                        weight = DEFAULT_WEIGHT;
                    }
                    movieService.setComponentWeight(movie, tag, weight);

                    System.out.print(".");
                } catch (Exception e) {
                    System.out.println("Failed to add tag; reason: " + e.getMessage());
                    continue;
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
