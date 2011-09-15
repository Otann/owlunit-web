package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.model.cinema.Movie;
import com.manymonkeys.model.cinema.Person;
import com.manymonkeys.model.cinema.Role;
import com.manymonkeys.service.impl.MovieServiceImpl;
import com.manymonkeys.service.impl.PersonServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rocket Science Software
 *
 * @author Ilya Pimenov
 * @author Anton Chebotaev
 */
public class ImdbPersonCrawler extends CassandraCrawler {

    public static final String DEFAULT_EMPTY_NAME = "";

    final Logger log = LoggerFactory.getLogger(ImdbPersonCrawler.class);

    private final String filePath;
    private final String role;
    private final double INITIAL_PERSON_WEIGHT;

    @Autowired
    MovieServiceImpl movieService;

    @Autowired
    PersonServiceImpl personService;

    Pattern PERSON_MOVIE_PATTERN = Pattern.compile("^([^\\t]+)\\t+(.+)\\((\\d+)\\).*$");
    Pattern MOVIE_PATTERN = Pattern.compile("^\\t\\t\\t(.+)\\((\\d+)\\).*$");

    public ImdbPersonCrawler(String filePath, double initialWeight, String role) {
        this.filePath = filePath;
        this.role = role;
        this.INITIAL_PERSON_WEIGHT = initialWeight;
    }

    public static void main(String[] args) {
        new ImdbPersonCrawler(args[0], Double.parseDouble(args[1]), args[2]).crawl();
    }

    public void run() throws Exception {

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));
        String line = fileReader.readLine();

        String name = null;
        String movie;

        String oldName = null;
        String oldMovie = null;

        Person person = null;

        int actorsCount = 0;
        TimeWatch timer = TimeWatch.start();

        while (line != null) {
            try {
                timer.tick(log, 10000, String.format("Found %d persons in file %s.", actorsCount, filePath), "lines");

                if ("".equals(line)) continue;

                Matcher personMovieMatcher;
                Matcher movieMatcher;

                if ((personMovieMatcher = PERSON_MOVIE_PATTERN.matcher(line)).matches()) {
                    name = personMovieMatcher.group(1).trim();
                    person = null;

                    movie = cropMovieName(personMovieMatcher.group(2));
                } else if ((movieMatcher = MOVIE_PATTERN.matcher(line)).matches()) {
                    movie = cropMovieName(movieMatcher.group(1)).trim();
                } else {
                    continue;
                }

                if (movie.equals(oldMovie) && name.equals(oldName)) {
                    continue;
                } else {
                    oldMovie = movie;
                    oldName = name;
                }

                Movie movieItem = movieService.loadByName(movie);
                if (movieItem == null)
                    continue;

                if (person == null) {
                    String[] fullname = splitName(name);
                    person = personService.findOrCreate(new Person(fullname[0], fullname[1], null, null), Role.valueOf(role));
                    actorsCount++;
                }

                movieService.addPerson(movieItem, person, Role.valueOf(role));

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                log.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
            } finally {
                line = fileReader.readLine();
            }
        }
        log.info(String.format("Processed %d persons all-in-all", actorsCount));
    }

    private String[] splitName(String fullname) {
        String name;
        String surname;
        if (fullname.indexOf(", ") > 0) {
            String[] parts = fullname.split(", ");
            name = parts[1];
            surname = parts[0];
        } else {
            name = DEFAULT_EMPTY_NAME;
            surname = name;
        }
        return new String[]{name, surname};
    }

    private String cropMovieName(String movieName) {
        String result = movieName.trim();
        if (result.charAt(0) == '"' || result.charAt(result.length() - 1) == '\'')
            result = result.substring(1);

        if (result.charAt(result.length() - 1) == '"' || result.charAt(result.length() - 1) == '\'')
            result = result.substring(0, result.length() - 1);

        return result;
    }

}
