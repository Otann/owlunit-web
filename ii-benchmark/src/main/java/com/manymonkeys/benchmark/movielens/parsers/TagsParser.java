package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.service.FastService;
import com.manymonkeys.benchmark.movielens.utils.TimeWatch;
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

    public static void parse(FastService service) throws IOException {
        String filePath = "../runtime/movielens/tags.dat";

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        TimeWatch watch = TimeWatch.start();

        String line = fileReader.readLine();
        while (line != null) {

            if ("".equals(line)) {
                continue;
            }

            try {
                String[] parts = line.split("\\:\\:");
                long userId = Long.parseLong(parts[0]);
                long movieId = Long.parseLong(parts[1]);
                String tagName = parts[2];

                InformationItem tag = service.loadOrCreateTag(tagName);

                InformationItem movie = service.loadMovie(movieId);
                service.scoreTagForMovie(movie, tag);

                InformationItem user = service.loadOrCreateUser(userId);
                service.scoreTagForUser(user, tag);

                watch.tick(1000, "Parsing tags.", "tag");

            } catch (Exception e) {
                System.out.println("Failed to add tag; reason: " + e.getMessage());
                e.printStackTrace();
            } finally {
                line = fileReader.readLine();
            }
        }
        System.out.println("All done");
    }

}
