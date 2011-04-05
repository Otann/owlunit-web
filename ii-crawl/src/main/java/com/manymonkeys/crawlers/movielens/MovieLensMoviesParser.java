package com.manymonkeys.crawlers.movielens;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;
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
 * @author Ilya Pimenov
 */
public class MovieLensMoviesParser {

    public static final String EXTERNAL_ID = MovieLensMoviesParser.class.getName() + ".EXTERNAL_ID";
    private static final String A_K_A = "a.k.a.";

    public static void main(String[] args) throws IOException {
        Cluster cluster = HFactory.getOrCreateCluster(
                PropertyManager.get(PropertyManager.Property.CASSANDRA_CLUSTER),
                PropertyManager.get(PropertyManager.Property.CASSANDRA_HOST));
        Keyspace keyspace = HFactory.createKeyspace(PropertyManager.get(PropertyManager.Property.CASSANDRA_KEYSPACE), cluster);

        try {
            MovieService movieService = new MovieService(keyspace);
            TagService tagService = new TagService(keyspace);

            String filePath = PropertyManager.get(PropertyManager.Property.MOVIES_DATA_FILE);
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

                InformationItem movie = movieService.createMovie(name, Long.parseLong(year));
                movieService.setMeta(movie, EXTERNAL_ID, id);
                if (aka != null) {
                    movieService.setMeta(movie, MovieService.AKA_NAME, aka);
                }
                if (nameTranslate != null) {
                    movieService.setMeta(movie, MovieService.TRANSLATE_NAME, nameTranslate);
                }

                done++;
                if (done % 25 == 0) {
                    System.out.println(String.format("Created %d movies", done));
                }

                for (String genre : genres) {
                    InformationItem tag = tagService.getTag(genre);
                    if (tag == null) {
                        tag = movieService.createTag(genre);
                    }
                    movieService.setComponentWeight(movie, tag, 1D); //TODO: Discuss Weight
                }

                line = fileReader.readLine();

            }

            System.out.println("All done");
        } finally {
            cluster.getConnectionManager().shutdown();
        }
    }

}