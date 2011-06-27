package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.service.FastMinRecommender;
import com.manymonkeys.benchmark.movielens.service.FastService;
import com.manymonkeys.benchmark.movielens.utils.TimeWatch;
import com.manymonkeys.core.ii.InformationItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class RatingsParser {

    static FastService service;
    static FastMinRecommender recommender = new FastMinRecommender();

    static long done = 0;
    public static final double MAGIC_MULTIPLICATOR = 500d;

    private static long over_below = 0;

    private static interface Processor {
        void processData(long userId, long movieId, double rating);
        String getName();
    }

    private static Processor trainingProcessor = new Processor() {
        @Override
        public void processData(long userId, long movieId, double rating) {
            InformationItem movie = service.loadMovie(movieId);
            InformationItem user = service.loadOrCreateUser(userId);
            if (movie == null) {
//                System.out.printf("Unable to find movie with id = %d%n", movieId);
                return;
            }
            if (user == null) {
//                System.out.printf("Unable to find user with id = %d%n", userId);
                return;
            }
            service.scoreMovieForUser(user, movie, rating);
        }

        @Override
        public String getName() {
            return "training processor";
        }
    };

    private static ResultData resultData = new ResultData();
    private static Processor testProcessor = new Processor() {
        double scaleRating(double rawRating) {
            return  rawRating * MAGIC_MULTIPLICATOR;
        }
        @Override
        public void processData(long userId, long movieId, double rating) {
            InformationItem movie = service.loadMovie(movieId);
            InformationItem user = service.loadOrCreateUser(userId);

            double comparationResult = scaleRating(recommender.compareItems(user, movie));
            if (comparationResult > 5) {
                comparationResult = 5;
            }
            resultData.sum += Math.pow(comparationResult - rating, 2);
            over_below = comparationResult > rating ? over_below + 1 : over_below - 1;
            if (resultData.count % 500 == 0) {
                System.out.printf("Current RMSE = %.3f ", resultData.getResilt());
                System.out.printf("Current comparation result was %.3f, actual %.0f. Over-below balance is %d.%n", comparationResult, rating, over_below);
            }
//        result.sum += Math.abs(comparationResult  - rating);

            resultData.count++;
        }
        @Override
        public String getName() {
            return "test processor";
        }
    };


    public static void parseFile(String filePath, Processor processor) throws IOException {
        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
        TimeWatch watch = TimeWatch.start();
        String line = fileReader.readLine();
        String tickLine = String.format("Processing ratings with %s.", processor.getName());
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

                processor.processData(userId, movieId, rating);

                watch.tick(5000, tickLine, "ratings");

            } catch (Exception e) {
                System.out.printf("Failed to process line %d, reason: %s%n", done, e.getMessage());
                e.printStackTrace();
            } finally {
                line = fileReader.readLine();
            }
        }
    }

    public static void parse(FastService service) throws IOException {

        RatingsParser.service = service;

        System.out.println("Parsing train file...");
        parseFile("../runtime/movielens/r1.train", trainingProcessor);

        System.out.println("Preparing dao for calculation...");
        recommender.prepareData(service);

        System.out.println("Parsing test file...");
        parseFile("../runtime/movielens/r1.test", testProcessor);

        double result = resultData.getResilt();
//        double result = resultData.sum / resultData.count;
        System.out.println(String.format("All done with ratings, RMSE = %.5f", result));
    }

    public static class ResultData {
        public long count = 0;
        public double sum = 0;

        public double getResilt() {
            return Math.sqrt(resultData.sum / resultData.count);
        }
    }

}
