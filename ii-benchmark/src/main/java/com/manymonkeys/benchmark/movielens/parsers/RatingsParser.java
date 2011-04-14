package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.service.FastMinRecommender;
import com.manymonkeys.benchmark.movielens.utils.TimeWatch;
import com.manymonkeys.benchmark.movielens.service.MovieLensService;
import com.manymonkeys.core.algo.Recommender;
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

    static MovieLensService service;
    static Recommender recommender = new FastMinRecommender();

    static long done = 0;

    static void processTrainingData(long userId, long movieId, double rating) throws Exception {
        InformationItem movie = service.getByMeta(MovieLensService.MOVIE_ID, Long.toString(movieId));
        if (movie == null) {
            throw new Exception(String.format("Failed to find movie with external Id = %d", movieId));
        }

        // Do user thing
        InformationItem user = service.loadOrCreateUser(userId);
        service.scoreMovieForUser(user, movie, rating);
    }

    static void processTestData(long userId, long movieId, double rating, ResultData result) throws Exception {
        InformationItem movie = service.getByMeta(MovieLensService.MOVIE_ID, Long.toString(movieId));
        if (movie == null) {
            throw new Exception(String.format("Failed to find movie with external Id = %d", movieId));
        }

        InformationItem user = service.getByMeta(MovieLensService.USER_ID, Long.toString(userId));
        if (user == null) {
            throw new Exception(String.format("Failed to find user with external Id = %d", movieId));
        }

        double comparationResult = recommender.compareItems(user, movie) * 350;
        if (comparationResult > 5) {
            comparationResult = 5;
        }
        result.sum += Math.pow(comparationResult * 5 - rating, 2);
//        result.sum += Math.abs(comparationResult  - rating);

        if (done % 10000 == 0) {
            System.out.println(String.format("Comparation result %.5f, actual result %.2f", comparationResult, rating));
        }

        result.count++;

    }

    public static void parseFile(String filePath, ResultData testResult) throws IOException {

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        String line = fileReader.readLine();
        done = 0;
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

                if (done % 2000 == 0) {
                    long passed = watch.time();
                    watch.reset();
                    System.out.println(String.format("Processed %d ratings. Going at speed %.3f / second", done, 2000d * 1000 / passed));
                }

            } catch (Exception e) {
                System.out.printf("Failed to process line %d, reason: %s%n", done, e.getMessage());
//                e.printStackTrace();
                //noinspection UnnecessaryContinue
                continue;
            } finally {
                line = fileReader.readLine();
            }

        }

    }

    public static void parse(MovieLensService service) throws IOException {

        RatingsParser.service = service;

        // Train set
        parseFile("../runtime/movielens/r1.train", null);

        // Test set
        ResultData resultData = new ResultData();
        parseFile("../runtime/movielens/r1.test", resultData);

        double result = Math.sqrt(resultData.sum / resultData.count);
//        double result = resultData.sum / resultData.count;
        System.out.println(String.format("All done with ratings, RMS = %.5f", result));
    }

    public static class ResultData {
        public long count = 0;
        public double sum = 0;
    }

}
