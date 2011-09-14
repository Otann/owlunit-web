package com.manymonkeys.crawlers.movielens;

import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.model.cinema.Keyword;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.service.cinema.KeywordService;
import com.manymonkeys.service.cinema.MovieService;
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
public class MovieLensTagsCrawler {

    public static final String EMPTY_STRING = "";

    final Logger log = LoggerFactory.getLogger(MovieLensTagsCrawler.class);

    @Autowired
    MovieService movieService;

    @Autowired
    KeywordService tagService;

    public static void main(String[] args) throws IOException {
        new MovieLensTagsCrawler().run(args[0]);
    }

    public void run(String filePath) throws IOException {

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));

        Map<String, Keyword> keywordLocalCache = new HashMap<String, Keyword>();
        Map<String, Movie> movieLocalCache = new HashMap<String, Movie>();

        TimeWatch watch = TimeWatch.start();

        String line = fileReader.readLine();
        Pattern p = Pattern.compile("\\:\\:(.*)\\:\\:");
        while (line != null) {
            if (EMPTY_STRING.equals(line))
                continue;

            try {
                Matcher matcher = p.matcher(line);
                matcher.find();
                String str = matcher.group(1);
                String externalId = str.substring(0, str.indexOf(':'));
                String tagName = str.substring(str.lastIndexOf(':') + 1, str.length()).toLowerCase();

                Movie movie = movieLocalCache.get(externalId);
                if (movie == null) {
                    movie = movieService.loadByExternalId(MovieLensMoviesCrawler.SERVICE_NAME, externalId);
                    movieLocalCache.put(externalId, movie);
                }

                watch.tick(log, 2000, "Processing movielens.", "tags");

                Keyword keyword = keywordLocalCache.get(tagName);
                if (keyword == null) {
                    keyword = tagService.createKeyword(tagName);
                    keywordLocalCache.put(tagName, keyword);
                }

                // Todo Ilya Pimenov this can be very slow, cuz it requires DB request. Better to keep it cached.
                // Or kill this parses completely, MovieLens tags are crap
                if (movieService.hasKeyword(movie, keyword)) {
                    movieService.addKeyword(movie, keyword);
                }

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
            } finally {
                line = fileReader.readLine();
            }

        }
        log.info("All done.");
    }

}
