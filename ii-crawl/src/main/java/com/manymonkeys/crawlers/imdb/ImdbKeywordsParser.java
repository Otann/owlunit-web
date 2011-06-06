package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.PropertyManager;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import me.prettyprint.hector.api.Keyspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 */
public class ImdbKeywordsParser extends CassandraCrawler {

    final Logger logger = LoggerFactory.getLogger(ImdbKeywordsParser.class);

    static final Pattern keywordCounter = Pattern.compile("([^\\s]+ \\(\\d+\\))");
    static final Pattern keywordLine    = Pattern.compile("^([^\\t]+) \\(\\d+\\)\\s+([^\\s]+)$");


    final double MIN_WEIGHT = Double.parseDouble(PropertyManager.get(PropertyManager.Property.IMDB_WEIGHT_KEYWORD_MIN));
    final double MAX_WEIGHT = Double.parseDouble(PropertyManager.get(PropertyManager.Property.IMDB_WEIGHT_KEYWORD_MAX));
    final double WEIGHT_RANGE = MAX_WEIGHT - MIN_WEIGHT;

    final int MAX_COUNT = Integer.parseInt(PropertyManager.get(PropertyManager.Property.IMDB_KEYWORD_COUNT_MAX));
    final int COUNT_THRESHOLD = Integer.parseInt(PropertyManager.get(PropertyManager.Property.IMDB_KEYWORD_COUNT_THRESHOLD));

    String filePath;

    public ImdbKeywordsParser(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        new ImdbKeywordsParser(args[0]).crawl();
    }

    @Override
    public void run(Keyspace keyspace) throws Exception {
        MovieService service = new MovieService(keyspace);

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));

        Map<String, Integer> keywordsCounts = parseCounts(fileReader);
        logger.info(String.format("Found %d keywords in file %s all-in-all, now parsing movies", keywordsCounts.size(), filePath));

        parseMovies(keywordsCounts, fileReader, service);

    }

    Map<String, Integer> parseCounts(BufferedReader reader) throws Exception {
        Map<String, Integer> result = new HashMap<String, Integer>();

        String line = reader.readLine();
        while(line != null && !line.contains("keywords in use"))
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

    void parseMovies(Map<String, Integer> keywordsCounts, BufferedReader reader, MovieService service) throws IOException {


        TimeWatch watch = TimeWatch.start();

        Map<String, UUID> localCache = new HashMap<String, UUID>();

        String line = reader.readLine();
        String oldMovieName = null;
        InformationItem movieItem = null;

        while (line != null) {
            try {

                watch.tick(logger, 100000, "Processing movies", "lines");

                Matcher matcher = keywordLine.matcher(line);
                if(!matcher.matches())
                    continue;

                String movieName = matcher.group(1);
                String keywordName = matcher.group(2);

                if (!movieName.equals(oldMovieName)) {
                    oldMovieName = movieName;
                    movieItem = service.getByNameSimplified(movieName);
                } else if (movieItem == null) {
                    // this means movie was not found previously
                    continue;
                }

                if (movieItem == null)
                    continue;

                InformationItem keywordItem = null;
                if (localCache.containsKey(keywordName)) {
                    keywordItem = service.loadByUUID(localCache.get(keywordName));
                } else {
                    keywordItem = service.createTag(keywordName);
                    localCache.put(keywordName, keywordItem.getUUID());
                }

                Integer count = keywordsCounts.get(keywordName);
                if (count == null)
                    continue;
                double weight = count >= MAX_COUNT ?
                        MIN_WEIGHT :
                        MAX_WEIGHT - (1d * count / MAX_COUNT) * WEIGHT_RANGE;

                service.setComponentWeight(movieItem, keywordItem, weight);

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
