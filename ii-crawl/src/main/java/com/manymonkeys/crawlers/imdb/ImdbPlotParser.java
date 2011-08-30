package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.core.ii.Ii;
import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import me.prettyprint.hector.api.Keyspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
public class ImdbPlotParser extends CassandraCrawler {

    final Logger logger = LoggerFactory.getLogger(ImdbPlotParser.class);

    @Autowired
    MovieService service;

    static final Pattern movieName = Pattern.compile("^MV: (.+) \\(\\d+\\).*$");
    static final Pattern plotLine = Pattern.compile("PL: (.+)$");
    static final Pattern authorLine = Pattern.compile("BY: (.+)$");

    String filePath;

    public ImdbPlotParser(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        new ImdbPlotParser(args[0]).crawl();
    }

    @Override
    public void run() throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));

        parseMovies(reader, service);

    }

    void parseMovies(BufferedReader reader, MovieService service) throws IOException {

        TimeWatch watch = TimeWatch.start();

        String line = reader.readLine();
        Ii movieItem = null;
        StringBuffer buffer = new StringBuffer();

        while (line != null) {
            try {

                watch.tick(logger, 100000, "Processing taglines", "lines");

                Matcher matcher;
                if ((matcher = movieName.matcher(line)).matches()) {
                    if (movieItem != null) {
                        service.createOrUpdateDescription(movieItem, buffer.toString());
                        buffer = new StringBuffer();
                    }

                    String movieName = matcher.group(1);
                    movieItem = service.getByNameSimplified(movieName);
                } else if ((matcher = plotLine.matcher(line)).matches()) {
                    if (movieItem == null)
                        continue;

                    buffer.append(matcher.group(1));
                    buffer.append(' ');
                } else if ((matcher = plotLine.matcher(line)).matches()) {
                    if (movieItem == null)
                        continue;

                    buffer.append("author :");
                    buffer.append(matcher.group(1));
                    buffer.append('\n');
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

        if (movieItem != null) {
            service.createOrUpdateDescription(movieItem, buffer.toString());
        }

    }
}
