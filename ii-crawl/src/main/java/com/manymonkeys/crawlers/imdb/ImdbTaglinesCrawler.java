package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 * @author Ilya Pimenov
 */
public class ImdbTaglinesCrawler extends CassandraCrawler {

    final Logger log = LoggerFactory.getLogger(ImdbTaglinesCrawler.class);

    MovieService movieService = (MovieService) ctx.getBean("movieService");

    static final Pattern MOVIE_NAME = Pattern.compile("^# (.+) \\((\\d+)\\)$");
    static final Pattern TAGLINE = Pattern.compile("^\t(.+)$");

    private String filePath;

    public ImdbTaglinesCrawler(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        new ImdbTaglinesCrawler(args[0]).crawl();
    }

    @Override
    public void run() throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));

        String line = reader.readLine();
        while (line != null && !line.contains("TAG LINES LIST"))
            line = reader.readLine();

        parse(reader);

    }

    void parse(BufferedReader reader) throws IOException {

        TimeWatch watch = TimeWatch.start();

        String line = reader.readLine();
        Movie movieItem = null;
        StringBuffer buffer = new StringBuffer();

        while (line != null) {
            try {

                watch.tick(log, 100000, "Processing taglines.", "lines");

                Matcher matcher;
                if ((matcher = MOVIE_NAME.matcher(line)).matches()) {
                    if (movieItem != null) {
                        movieService.addTagline(movieItem, buffer.toString());
                        buffer = new StringBuffer();
                    }

                    String movieName = matcher.group(1);
                    long year = Long.parseLong(matcher.group(2));
                    try {
                        movieItem = movieService.loadByName(movieName, year);
                    } catch (NotFoundException e) {
                        movieItem = null;
                    }
                } else if ((matcher = TAGLINE.matcher(line)).matches()) {
                    if (movieItem == null)
                        continue;

                    buffer.append(matcher.group(1));
                    buffer.append("\n");
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
            try {
                movieService.addTagline(movieItem, buffer.toString());
            } catch (NotFoundException e) {
                log.error("Loaded object can't be found for update, %s", movieItem.toString());
            }
        }
    }

}
