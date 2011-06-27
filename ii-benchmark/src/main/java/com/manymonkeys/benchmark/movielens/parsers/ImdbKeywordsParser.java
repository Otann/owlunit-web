package com.manymonkeys.benchmark.movielens.parsers;

import com.manymonkeys.benchmark.movielens.service.FastService;
import com.manymonkeys.benchmark.movielens.utils.TimeWatch;
import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.service.cinema.MovieService;
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
public class ImdbKeywordsParser extends ImdbParser {

    final Logger logger = LoggerFactory.getLogger(ImdbKeywordsParser.class);

    static final Pattern keywordCounter = Pattern.compile("([^\\s]+ \\(\\d+\\))");
    static final Pattern keywordLine    = Pattern.compile("^([^\\t]+) \\(\\d+\\)\\s+([^\\s]+)$");

    final double MIN_WEIGHT = Constants.MIN_KEYWORD_WEIGHT;
    final double MAX_WEIGHT = Constants.MAX_KEYWORD_WEIGHT;
    final double WEIGHT_RANGE = MAX_WEIGHT - MIN_WEIGHT;

    final int MAX_COUNT = Constants.MAX_KEYWORD_COUNT;
    final int COUNT_THRESHOLD = Constants.KEYWORD_THRESHOLD;

    String filePath;

    public ImdbKeywordsParser(String filePath, FastService service) {
        super(service);
        this.filePath = filePath;
    }

    @Override
    public void run() throws Exception {
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
        return result;
    }

    void parseMovies(Map<String, Integer> keywordsCounts, BufferedReader reader, FastService service) throws IOException {
        TimeWatch watch = TimeWatch.start();

        String line = reader.readLine();
        String oldMovieName = null;
        InformationItem movieItem = null;

        while (line != null) {
            try {
                watch.tick(logger, 100000, "Processing keywords for movies.", "lines");

                Matcher matcher = keywordLine.matcher(line);
                if(!matcher.matches())
                    continue;

                String movieName = matcher.group(1);
                String keywordName = matcher.group(2);

                if (!movieName.equals(oldMovieName)) {
                    oldMovieName = movieName;
                    movieItem = service.getByNameSimplified(movieName);
                }

                if (movieItem == null)
                    continue;

                InformationItem keywordItem = service.loadOrCreateTag(keywordName);
                Integer count = keywordsCounts.get(keywordName);
                if (count == null)
                    continue;
                double weight = count >= MAX_COUNT ?
                        MIN_WEIGHT :
                        MAX_WEIGHT - WEIGHT_RANGE * count / MAX_COUNT;

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
