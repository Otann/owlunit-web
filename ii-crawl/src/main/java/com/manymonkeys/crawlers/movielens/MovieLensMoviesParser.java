package com.manymonkeys.crawlers.movielens;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.index.IndexService;
import org.neo4j.index.lucene.LuceneFulltextQueryIndexService;
import org.neo4j.kernel.EmbeddedGraphDatabase;

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
public class MovieLensMoviesParser {

    public static final String EXTERNAL_ID = MovieLensMoviesParser.class.getName() + ".EXTERNAL_ID";
    public static final String ALTERNATE_NAME = MovieLensMoviesParser.class.getName() + ".AKA_NAME";

    public static void main(String[] args) throws IOException {
        File dbFile = new File(PropertyManager.get(PropertyManager.Property.NEO4J_DB));
        System.out.println("Database: " + dbFile.getAbsolutePath());

        GraphDatabaseService graphDb = new EmbeddedGraphDatabase(dbFile.getAbsolutePath());
        IndexService indexService = new LuceneFulltextQueryIndexService(graphDb);

        try {
            MovieService movieService = new MovieService();
            movieService.setGraphDb(graphDb);
            movieService.setIndexService(indexService);

            TagService tagService = new TagService();
            tagService.setGraphDb(graphDb);
            tagService.setIndexService(indexService);

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
                String name = line.substring(fistSemicolon + 2, lastSemicolon - 2 - 6);
                String year = line.substring(lastSemicolon - 6, lastSemicolon - 2);
                String[] genres = line.substring(lastSemicolon + 1, line.length()).split("\\|");

                InformationItem movie = movieService.createMovie(name, Long.parseLong(year));
                movieService.setMeta(movie, EXTERNAL_ID, id);

                done++;
                if (done % 25 == 0) {
                    System.out.println(String.format("Created %d movies", done));
                }

                for (String genre : genres) {
                    InformationItem tag = tagService.getTag(genre);
                    if (tag == null) {
                        tag = movieService.createTag(genre);
                    }
                    movieService.setComponentWeight(movie, tag, 1); //TODO: Discuss Weight
                }

                line = fileReader.readLine();

            }
            int count = 0;
            for (Node n : graphDb.getAllNodes()) {
                count++;
            }
            System.out.println("All-in-all " + count + " nodes");
        } finally {
            graphDb.shutdown();
            indexService.shutdown();
        }
    }

}
