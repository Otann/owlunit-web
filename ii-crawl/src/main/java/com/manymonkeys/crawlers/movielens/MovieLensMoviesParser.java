package com.manymonkeys.crawlers.movielens;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.crawlers.common.PropertyManager;
import com.manymonkeys.crawlers.common.PropertyManager.Property;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class MovieLensMoviesParser {

    final Logger logger = LoggerFactory.getLogger(MovieLensMoviesParser.class);

    public static final String EXTERNAL_ID = MovieLensMoviesParser.class.getName() + ".EXTERNAL_ID";
    private static final String A_K_A = "a.k.a.";

    public final double INITIAL_GENRE_WEIGHT = Double.parseDouble(PropertyManager.get(Property.MOVIELENS_GENRE_WEIGHT_INITIAL));

    private static Map<String, InformationItem> localYearsCache = new HashMap<String, InformationItem>();

    public static void main(String[] args) throws IOException {
        new MovieLensMoviesParser().run(args[0]);
    }

    public void run(String filePath) throws IOException, UnsupportedEncodingException {

        Cluster cluster = HFactory.getOrCreateCluster(
                PropertyManager.get(Property.CASSANDRA_CLUSTER),
                PropertyManager.get(Property.CASSANDRA_HOST));
        Keyspace keyspace = HFactory.createKeyspace(PropertyManager.get(PropertyManager.Property.CASSANDRA_KEYSPACE), cluster);

        try {
            MovieService movieService = new MovieService(keyspace);
            TagService tagService = new TagService(keyspace);

            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));

            String line = fileReader.readLine();

            TimeWatch watch = TimeWatch.start();

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
                movieService.setMeta(movie, EXTERNAL_ID, id, true);
                if (aka != null) {
                    movieService.setMeta(movie, MovieService.AKA_NAME, aka, true);
                }
                if (nameTranslate != null) {
                    movieService.setMeta(movie, MovieService.TRANSLATE_NAME, nameTranslate, true);
                }

//                InformationItem yearItem;
//                if (localYearsCache.containsKey(year)) {
//                    yearItem = localYearsCache.get(year);
//                } else {
//                    yearItem = tagService.createTag(year);
//                    localYearsCache.put(year, yearItem);
//                }
//                movieService.setComponentWeight(movie, yearItem, INITIAL_YEAR_WEIGHT);

                watch.tick(logger, 100, "Crawling movielens.", "movies");

                for (String genre : genres) {
                    InformationItem tag = tagService.getTag(genre);
                    if (tag == null) {
                        tag = movieService.createTag(genre);
                    }
                    movieService.setComponentWeight(movie, tag, INITIAL_GENRE_WEIGHT);
                }

                line = fileReader.readLine();

            }

            System.out.println("All done");
        } finally {
            cluster.getConnectionManager().shutdown();
        }
    }

}
