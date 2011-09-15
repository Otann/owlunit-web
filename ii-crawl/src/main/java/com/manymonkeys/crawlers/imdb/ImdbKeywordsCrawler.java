package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.PropertyManager;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class ImdbKeywordsCrawler extends CassandraCrawler {

    @Autowired
    MovieService movieService;

    @Autowired
    KeywordService keywordService;

    final Logger logger = LoggerFactory.getLogger(ImdbKeywordsCrawler.class);

    static final Pattern keywordCounter = Pattern.compile("([^\\s]+ \\(\\d+\\))");
    static final Pattern keywordLine = Pattern.compile("^([^\\t]+) \\(\\d+\\)\\s+([^\\s]+)$");


    final double MIN_WEIGHT = Double.parseDouble(PropertyManager.get(PropertyManager.Property.IMDB_WEIGHT_KEYWORD_MIN));
    final double MAX_WEIGHT = Double.parseDouble(PropertyManager.get(PropertyManager.Property.IMDB_WEIGHT_KEYWORD_MAX));
    final double WEIGHT_RANGE = MAX_WEIGHT - MIN_WEIGHT;

    final int MAX_COUNT = Integer.parseInt(PropertyManager.get(PropertyManager.Property.IMDB_KEYWORD_COUNT_MAX));
    final int COUNT_THRESHOLD = Integer.parseInt(PropertyManager.get(PropertyManager.Property.IMDB_KEYWORD_COUNT_THRESHOLD));

    String filePath;

    public ImdbKeywordsCrawler(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        new ImdbKeywordsCrawler(args[0]).crawl();
    }

    @Override
    public void run() throws Exception {

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));

        Map<String, Integer> keywordsCounts = parseCounts(fileReader);
        logger.info(String.format("Found %d keywords in file %s all-in-all, now parsing movies", keywordsCounts.size(), filePath));

        parseMovies(keywordsCounts, fileReader);

    }

    Map<String, Integer> parseCounts(BufferedReader reader) throws Exception {
        Map<String, Integer> result = new HashMap<String, Integer>();

        String line = reader.readLine();
        while (line != null && !line.contains("keywords in use"))
            line = reader.readLine();

        if (line == null)
            throw new Exception("Unexpected end of file");

        line = reader.readLine();
        while (line != null && !line.contains("THE KEYWORDS LIST")) {
            try {

                Matcher matcher = keywordCounter.matcher(line);
                while (matcher.find()) {
                    String match = matcher.group();
                    String name = match.substring(0, match.indexOf(' '));
                    int value = Integer.parseInt(match.substring(match.lastIndexOf('(') + 1, match.lastIndexOf(')')));
                    if (value < COUNT_THRESHOLD)
                        continue;
                    result.put(name, value);
                }

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
            } finally {
                line = reader.readLine();
            }
        }

        if (line == null)
            throw new Exception("Unexpected end of file");

        return result;
    }

    void parseMovies(Map<String, Integer> keywordsCounts, BufferedReader reader) throws IOException {

        TimeWatch watch = TimeWatch.start();

        Map<String, UUID> localCache = new HashMap<String, UUID>();

        String line = reader.readLine();
        String oldMovieName = null;
        Movie movie = null;

        while (line != null) {
            try {
                watch.tick(logger, 100000, "Processing movies", "lines");

                Matcher matcher = keywordLine.matcher(line);
                if (!matcher.matches()) {
                    continue;
                }

                String movieName = matcher.group(1);
                String keywordName = matcher.group(2);

                if (!movieName.equals(oldMovieName)) {
                    oldMovieName = movieName;
                    movie = movieService.loadByName(movieName);
                } else if (movie == null) {
                    // this means movie was not found previously
                    continue;
                }

                if (movie == null)
                    continue;

                Keyword keyword = null;
                if (localCache.containsKey(keywordName)) {
                    keyword = keywordService.loadByUUID(localCache.get(keywordName));
                } else {
                    keyword = keywordService.createKeyword(keywordName);
                    localCache.put(keywordName, keyword.getUuid());
                }

                Integer count = keywordsCounts.get(keywordName);
                if (count == null) {
                    continue;
                }
                double frequency = 1d * count / MAX_COUNT;
                movieService.addKeyword(movie, keyword);

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
            } finally {
                line = reader.readLine();
            }
        }

    }
}
