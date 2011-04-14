package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.utils.PropertyManager;
import com.manymonkeys.benchmark.movielens.service.MovieLensService;
import com.manymonkeys.core.ii.InformationItem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class MoviesParser {

    public static String filePath = PropertyManager.get(PropertyManager.Property.MOVIES_DATA_FILE);

    public static void parse(MovieLensService service) throws IOException {

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        String line = fileReader.readLine();
        long done = 0;
        while (line != null) {

            if ("".equals(line)) {
                continue;
            }

            int fistSemicolon = line.indexOf(':');
            String id = line.substring(0, fistSemicolon);
            int lastSemicolon = line.lastIndexOf(':');
            String name = line.substring(fistSemicolon + 2, lastSemicolon - 2 - 6).trim();

            String year = line.substring(lastSemicolon - 6, lastSemicolon - 2);
            String[] genres = line.substring(lastSemicolon + 1, line.length()).split("\\|");

            InformationItem movie = service.createMovie(name, Long.parseLong(year));
            service.setMeta(movie, MovieLensService.MOVIE_ID, id);

            done++;
            if (done % 500 == 0) {
                System.out.println(String.format("Created %d movies", done));
            }

            for (String genre : genres) {
                InformationItem tag = service.getTag(genre);
                if (tag == null) {
                    tag = service.createTag(genre);
                }
                service.scoreTag(movie, tag);
            }

            line = fileReader.readLine();

        }

        System.out.println("All done");
    }

}
