package com.manymonkeys.benchmark.movielens;

import com.manymonkeys.benchmark.movielens.parsers.MoviesParser;
import com.manymonkeys.benchmark.movielens.parsers.RatingsParser;
import com.manymonkeys.benchmark.movielens.parsers.TagsParser;
import com.manymonkeys.benchmark.movielens.service.MovieLensService;

import java.io.IOException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class DatasetRunner {

    public static void main(String[] args) throws IOException {

        MovieLensService service = new MovieLensService();

        MoviesParser.parse(service);
        TagsParser.parse(service);

        RatingsParser.parse(service);

    }
}
