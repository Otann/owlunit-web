package com.manymonkeys.crawlers.movielens;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.crawlers.common.PropertyManager;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.factory.HFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many Monkeys
 *
 * @author Ilya Pimenov
 */
public class MovieLensTagsParser {

    final Logger logger = LoggerFactory.getLogger(MovieLensTagsParser.class);

    @Autowired
    MovieService movieService;

    @Autowired
    TagService tagService;

    public static void main(String[] args) throws IOException {
        new MovieLensTagsParser().run(args[0]);
    }

    public void run(String filePath) throws IOException {

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));

        Map<String, Ii> tagCache = new HashMap<String, Ii>();
        Map<String, Ii> moviesCache = new HashMap<String, Ii>();

        TimeWatch watch = TimeWatch.start();

        String line = fileReader.readLine();
        Pattern p = Pattern.compile("\\:\\:(.*)\\:\\:");
        while (line != null) {
            if ("".equals(line))
                continue;

            try {
                Matcher matcher = p.matcher(line);
                matcher.find();
                String str = matcher.group(1);
                String externalId = str.substring(0, str.indexOf(':'));
                String tagName = str.substring(str.lastIndexOf(':') + 1, str.length()).toLowerCase();

                Ii movieItem = moviesCache.get(externalId);
                if (movieItem == null) {
                    movieItem = movieService.loadByExternalId(MovieLensMoviesParser.SERVICE_NAME, externalId);
                    moviesCache.put(externalId, movieItem);
                }

                watch.tick(logger, 2000, "Processing movielens.", "tags");

                Ii tagItem = tagCache.get(tagName);
                if (tagItem == null) {
                    tagItem = tagService.createTag(tagName);
                    tagCache.put(tagName, tagItem);
                }

                Double weight = movieItem.getComponentWeight(tagItem);
                if (weight == null) {
                    movieService.addKeyword(movieItem, tagItem);
                }
//                    else {
//                        movieService.addKeyword(movieItem, tagItem);
//                    }

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
            } finally {
                line = fileReader.readLine();
            }

        }

        logger.info("All done");
    }

}
