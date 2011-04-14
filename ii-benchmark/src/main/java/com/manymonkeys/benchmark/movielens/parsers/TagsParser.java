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
 * @author Ilya Pimenov
 */
public class TagsParser {

    public static void parse(MovieLensService service) throws IOException {
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
//                long userId = Long.parseLong(parts[0]);
                long movieId = Long.parseLong(parts[1]);
                String tagName = parts[2];

                InformationItem tag = service.getTag(tagName);
                if (tag == null) {
                    tag = service.createTag(tagName);
                }


                // Do movie thing
                InformationItem movie;
                movie = service.getByMeta(MovieLensService.MOVIE_ID, Long.toString(movieId));
                if (movie == null) {
                    System.out.printf("Failed to find movie with external Id = %d; %n", movieId);
                    continue;
                }
                service.scoreTag(movie, tag);

                // Do user thing
//                    InformationItem user = service.loadOrCreateUser(userId);
//                    service.scoreTag(user, tag);

                done++;
                if (done % 500 == 0) {
                    System.out.println(String.format("Created %d tags", done));
                }

            } catch (Exception e) {
                System.out.println("Failed to add tag; reason: " + e.getMessage());
                e.printStackTrace();
                //noinspection UnnecessaryContinue
                continue;
            } finally {
                line = fileReader.readLine();
            }

        }

        System.out.println("All done");
    }

}
