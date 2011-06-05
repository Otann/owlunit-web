package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.PropertyManager;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import me.prettyprint.hector.api.Keyspace;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class ImdbTaglinesParser extends CassandraCrawler {

    static final Pattern movieName = Pattern.compile("^# (.+) \\(\\d+\\)$");
    static final Pattern tagline   = Pattern.compile("^\t(.+)$");

    String filePath;

    public ImdbTaglinesParser(String filePath) {
        this.filePath = filePath;
    }

    public static void main(String[] args) {
        new ImdbTaglinesParser(args[0]).crawl();
    }

    @Override
    public void run(Keyspace keyspace) throws Exception {
        MovieService service = new MovieService(keyspace);

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));

        String line = reader.readLine();
        while(line != null && !line.contains("TAG LINES LIST"))
            line = reader.readLine();

        parseMovies(reader, service);

    }

    void parseMovies(BufferedReader reader, MovieService service) throws IOException {


        TimeWatch watch = TimeWatch.start();

        String line = reader.readLine();
        InformationItem movieItem = null;
        StringBuffer buffer = new StringBuffer();

        while (line != null) {
            try {

                watch.tick(100000, "Processing taglines", "lines");

                Matcher matcher;
                if ((matcher = movieName.matcher(line)).matches()) {
                    if (movieItem != null) {
                        service.setMeta(movieItem, MovieService.TAGLINES, buffer.toString());
                        buffer = new StringBuffer();
                    }

                    String movieName = matcher.group(1);
                    movieItem = service.getByNameSimplified(movieName);
                } else if ((matcher = tagline.matcher(line)).matches()) {
                    if (movieItem == null)
                        continue;

                    buffer.append(matcher.group(1));
                    buffer.append("\n");
                }

            } catch (Exception e) {
                System.out.printf("Unable to parse line %s. Exception: %s%n", line, e.getMessage());
                e.printStackTrace();
            } finally {
                line = reader.readLine();
            }
        }

        if (movieItem != null) {
            service.setMeta(movieItem, MovieService.TAGLINES, buffer.toString());
        }

    }
}
