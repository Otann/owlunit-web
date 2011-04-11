package com.manymonkeys.benchmark.movielens;

import com.manymonkeys.benchmark.PropertyManager;
import com.manymonkeys.benchmark.TimeWatch;
import com.manymonkeys.benchmark.movielens.service.MovieLensService;
import com.manymonkeys.core.algo.Recommender;
import com.manymonkeys.core.algo.impl.RecommenderPlainImpl;
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
 * @author Anton Chebotaev
 */
public class RatingsParser {

    static MovieLensService service;
    static Recommender recommender;

    static void processTrainingData(long userId, long movieId, double rating) throws Exception {
        InformationItem movie;
        try {
            movie = service.multigetByMeta(MovieService.MOVIE_LENS_ID, Long.toString(movieId)).iterator().next();
        } catch (Exception e) {
            throw new Exception("Failed to find movie with external Id = " + movieId, e);
        }

        // Do user thing
        InformationItem user = service.loadOrCreateUser(userId);
        service.scoreMovieForUser(user, movie, rating);
    }

    static void processTestData(long userId, long movieId, double rating, RMSEData result) throws Exception {
        InformationItem movie;
        try {
            movie = service.multigetByMeta(MovieService.MOVIE_LENS_ID, Long.toString(movieId)).iterator().next();
        } catch (Exception e) {
            throw new Exception("Failed to find movie with external Id = " + movieId, e);
        }

        InformationItem user;
        try {
            user = service.multigetByMeta(MovieLensService.USER_ID, Long.toString(userId)).iterator().next();
        } catch (Exception e) {
            throw new Exception("Failed to find user with external Id = " + movieId, e);
        }

        double comparationResult = recommender.compareItems(user, movie);
        result.squaresSum += Math.pow(comparationResult / 20 - rating, 2);
        result.count++;

    }

    public static void parseFile(String filePath, RMSEData testResult) throws IOException {

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        String line = fileReader.readLine();
        long done = 0;
        TimeWatch watch = TimeWatch.start();

        while (line != null) {
            done++;

            if ("".equals(line)) {
                continue;
            }

            try {
                String[] parts = line.split("\\:\\:");
                long userId = Long.parseLong(parts[0]);
                long movieId = Long.parseLong(parts[1]);
                double rating = Double.parseDouble(parts[2]);

                if (testResult == null) {
                    processTrainingData(userId, movieId, rating);
                } else {
                    processTestData(userId, movieId, rating, testResult);
                }

                System.out.print(".");

                if (done % 25 == 0) {
                    long passed = watch.time();
                    watch.reset();
                    System.out.println(String.format("Processed %d ratings. Going at speed %.3f / second", done, 25d * 1000 / passed));
                }

            } catch (Exception e) {
                System.out.println("Failed to process line, reason: " + e.getMessage());
                e.printStackTrace();
                continue;
            } finally {
                line = fileReader.readLine();
            }

        }

    }

    public static void main(String[] args) throws IOException {
        Cluster cluster = HFactory.getOrCreateCluster(
                PropertyManager.get(PropertyManager.Property.CASSANDRA_CLUSTER),
                PropertyManager.get(PropertyManager.Property.CASSANDRA_HOST));
        Keyspace keyspace = HFactory.createKeyspace(PropertyManager.get(PropertyManager.Property.CASSANDRA_KEYSPACE), cluster);

        service = new MovieLensService(keyspace);
        recommender = new RecommenderPlainImpl();

        try {

            parseFile("../runtime/movielens/r1.train", null);

            RMSEData resultData = new RMSEData();
            parseFile("../runtime/movielens/r1.test", resultData);

            double result = Math.sqrt(resultData.squaresSum / resultData.count);

            System.out.println(String.format("All done with ratings, RMS = %.5f", result));
        } finally {
            cluster.getConnectionManager().shutdown();
        }
    }

    public static class RMSEData {
        public long count = 0;
        public double squaresSum = 0;
    }

}
