package com.manymonkeys.benchmark.movielens;

import com.manymonkeys.benchmark.PropertyManager;
import com.manymonkeys.benchmark.movielens.service.MovieLensService;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Many Monkeys
 *
 * @author Ilya Pimenov
 */
public class TagsParser {

    private static long DEFAULT_WEIGHT = 1;

    public static void main(String[] args) throws IOException {
        Cluster cluster = HFactory.getOrCreateCluster(
                PropertyManager.get(PropertyManager.Property.CASSANDRA_CLUSTER),
                PropertyManager.get(PropertyManager.Property.CASSANDRA_HOST));
        Keyspace keyspace = HFactory.createKeyspace(PropertyManager.get(PropertyManager.Property.CASSANDRA_KEYSPACE), cluster);

        try {

            MovieLensService service = new MovieLensService(keyspace);

            String filePath = PropertyManager.get(PropertyManager.Property.TAGS_DATA_FILE);
            BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

            String line = fileReader.readLine();
            long done = 0;
            while (line != null) {

                if ("".equals(line)) {
                    continue;
                }

                try {
                    String[] parts = line.split("\\:\\:");
                    long userId = Long.parseLong(parts[0]);
                    long movieId = Long.parseLong(parts[1]);
                    String tagName = parts[2];

                    InformationItem tag = service.getTag(tagName);
                    if (tag == null) {
                        tag = service.createTag(tagName);
                    }


                    // Do movie thing
                    InformationItem movie;
                    try {
                        movie = service.multigetByMeta(MovieService.MOVIE_LENS_ID, Long.toString(movieId)).iterator().next();
                    } catch (Exception e) {
                        System.out.println("Failed to find movie with external Id = " + movieId + "; " + e.getMessage());
                        e.printStackTrace();
                        continue;
                    }
                    service.scoreTag(movie, tag);

                    // Do user thing
//                    InformationItem user = service.loadOrCreateUser(userId);
//                    service.scoreTag(user, tag);

                    System.out.print(".");

                    done++;
                    if (done % 25 == 0) {
                        System.out.println(String.format("Created %d tags", done));
                    }

                } catch (Exception e) {
                    System.out.println("Failed to add tag; reason: " + e.getMessage());
                    e.printStackTrace();
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
