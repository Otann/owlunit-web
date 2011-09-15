package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.service.cinema.MovieService;
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
public class ImdbPlotCrawler extends CassandraCrawler {

    final Logger log = LoggerFactory.getLogger(ImdbPlotCrawler.class);

    @Autowired
    MovieService movieService;

    static final Pattern MOVIE_NAME = Pattern.compile("^MV: (.+) \\(\\d+\\).*$");
    static final Pattern PLOT_LINE = Pattern.compile("PL: (.+)$");

    private String filePath;

    public ImdbPlotCrawler(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        new ImdbPlotCrawler(args[0]).crawl();
    }

    @Override
    public void run() throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));

        parseMovies(reader);
    }

    private void parseMovies(BufferedReader reader) throws IOException {
        TimeWatch watch = TimeWatch.start();

        String line = reader.readLine();
        Movie movieItem = null;
        StringBuffer buffer = new StringBuffer();

        while (line != null) {
            try {

                watch.tick(log, 100000, "Processing taglines", "lines");

                Matcher matcher;
                if ((matcher = MOVIE_NAME.matcher(line)).matches()) {
                    if (movieItem != null) {
                        movieService.setDescription(movieItem, buffer.toString());
                        buffer = new StringBuffer();
                    }

                    String movieName = matcher.group(1);
                    movieItem = movieService.loadByName(movieName);
                } else if ((matcher = PLOT_LINE.matcher(line)).matches()) {
                    if (movieItem == null)
                        continue;

                    buffer.append(matcher.group(1));
                    buffer.append(' ');
                } else if ((matcher = PLOT_LINE.matcher(line)).matches()) {
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
                log.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
            } finally {
                line = reader.readLine();
            }
        }

        if (movieItem != null) {
            movieService.setDescription(movieItem, buffer.toString());
        }
    }

}
