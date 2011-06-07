
package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.PersonService;
import me.prettyprint.hector.api.Keyspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rocket Science Software
 *
 * @author Ilya Pimenov
 * @author Anton Chebotaev
 */
public class ImdbPersonParser extends CassandraCrawler {

    final Logger logger = LoggerFactory.getLogger(ImdbPersonParser.class);

    private final String filePath;
    private final String role;
    private final double INITIAL_PERSON_WEIGHT;

    MovieService movieService;
    PersonService personService;

    Pattern personMoviePattern = Pattern.compile("^([^\\t]+)\\t+(.+)\\((\\d+)\\).*$");
    Pattern moviePattern =       Pattern.compile("^\\t\\t\\t(.+)\\((\\d+)\\).*$");

    public ImdbPersonParser(String filePath, double initialWeight, String role) {
        this.filePath = filePath;
        this.role = role;
        this.INITIAL_PERSON_WEIGHT = initialWeight;
    }


    public static void main(String[] args) {
        new ImdbPersonParser(args[0], Double.parseDouble(args[1]), args[2]).crawl();
    }

    public void run(Keyspace keyspace) throws Exception {

        movieService = new MovieService(keyspace);
        personService = new PersonService(keyspace);

        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));
        String line = fileReader.readLine();

        // Init strings
        String name = null;
        String movie;

        String oldName = null;
        String oldMovie = null;

//        String year = null;

        InformationItem personItem = null;

        int actorsCount = 0;
        TimeWatch timer = TimeWatch.start();

        while (line != null) {
            try {
                timer.tick(logger, 10000, String.format("Found %d persons in file %s.", actorsCount, filePath), "lines");

                if ("".equals(line)) continue;

                Matcher personMovieMatcher;
                Matcher movieMatcher;

                if ((personMovieMatcher = personMoviePattern.matcher(line)).matches()) {
                    name = personMovieMatcher.group(1).trim();
                    personItem = null;

                    movie = cropMovieName(personMovieMatcher.group(2));
//                    year = personMovieMatcher.group(3).trim();
                } else if ((movieMatcher = moviePattern.matcher(line)).matches()) {
                    movie = cropMovieName(movieMatcher.group(1)).trim();
//                    year = movieMatcher.group(2).trim();
                } else {
                    continue;
                }

                if (movie.equals(oldMovie) && name.equals(oldName)) {
                    continue;
                } else {
                    oldMovie = movie;
                    oldName = name;
                }

                InformationItem movieItem = movieService.getByNameSimplified(movie);
                if (movieItem == null)
                    continue;

                if (personItem == null) {
                    personItem = processPerson(name);
                    actorsCount++;
                }

                processMovie(movieItem, personItem);

            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                logger.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
            } finally {
                line = fileReader.readLine();
            }
        }
        logger.info(String.format("Processed %d persons all-in-all", actorsCount));
    }

    InformationItem processPerson(String name) {
        String firstName;
        String lastName;
        if (name.indexOf(", ") > 0) {
            String[] parts = name.split(", ");
            firstName = parts[1];
            lastName = parts[0];
        } else {
            firstName = "";
            lastName = name;
        }
        InformationItem person = personService.getPerson(firstName, lastName);
        if (person == null) {
            person = personService.createPerson(firstName, lastName);
        }
        personService.addRole(person, role);
        return person;
    }

    void processMovie(InformationItem movie, InformationItem person) {
        movieService.setComponentWeight(movie, person, INITIAL_PERSON_WEIGHT);
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
