package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.service.MovieLensService;
import com.manymonkeys.benchmark.movielens.utils.TimeWatch;
import com.manymonkeys.core.ii.InformationItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class MoviesParser {

    public static String filePath = "../runtime/movielens/movies.dat";

    public static void parse(MovieLensService service) throws IOException {

        TimeWatch watch = TimeWatch.start();

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
        String line = fileReader.readLine();
        while (line != null) {
            if ("".equals(line)) {
                continue;
            }

            int fistSemicolon = line.indexOf(':');
            long externalId = Long.parseLong(line.substring(0, fistSemicolon));
            int lastSemicolon = line.lastIndexOf(':');
            String name = line.substring(fistSemicolon + 2, lastSemicolon - 2 - 6).trim();

            String year = line.substring(lastSemicolon - 6, lastSemicolon - 2);
            String[] genres = line.substring(lastSemicolon + 1, line.length()).split("\\|");

            InformationItem movie = service.loadOrCreateMovie(externalId);

            watch.tick(1000, "Creating movies.", "movies");

            for (String genre : genres) {
                InformationItem tag = service.loadOrCreateTag(genre);
                service.scoreTagForMovie(movie, tag);
            }

            line = fileReader.readLine();
        }
        System.out.println("All done");
    }

}
