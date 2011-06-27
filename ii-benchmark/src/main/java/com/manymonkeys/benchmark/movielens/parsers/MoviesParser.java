package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.service.FastService;
import com.manymonkeys.benchmark.movielens.utils.TimeWatch;
import com.manymonkeys.core.ii.InformationItem;

import java.io.*;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class MoviesParser {

    public static String filePath = "../runtime/movielens/movies.dat";

    private static final String A_K_A = "a.k.a.";

    public static void parse(FastService service) throws IOException {

        TimeWatch watch = TimeWatch.start();

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
        String line = fileReader.readLine();
        while (line != null) {
            if ("".equals(line)) {
                continue;
            }

            int fistSemicolon = line.indexOf(':');
            long externalId = Long.parseLong(line.substring(0, fistSemicolon));
            int lastSemicolon = line.lastIndexOf(':');
            String name = line.substring(fistSemicolon + 2, lastSemicolon - 2 - 6).trim();
            String nameTranslate = null;
            String aka = null;
            if (name.charAt(name.length() - 1) == ')'){
                nameTranslate = name.substring(name.indexOf("(") + 1, name.length() - 1);
                name = name.substring(0, name.indexOf("(")).trim();
                if (nameTranslate.startsWith(A_K_A)){
                    aka = nameTranslate.substring(A_K_A.length(), nameTranslate.length()).trim();
                    nameTranslate = null;
                }
            }
            String year = line.substring(lastSemicolon - 6, lastSemicolon - 2);
            String[] genres = line.substring(lastSemicolon + 1, line.length()).split("\\|");

            InformationItem movie = service.createMovie(externalId, name);

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
