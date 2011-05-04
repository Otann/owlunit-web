
package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.PropertyManager;
import com.manymonkeys.crawlers.common.TimeWatch;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.PersonService;
import com.manymonkeys.service.cinema.TagService;
import me.prettyprint.hector.api.Keyspace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rocket Science Software
 *
 * @author Ilya Pimenov
 */
public class ImdbActorsParser extends CassandraCrawler {

    private static long DEFAULT_WEIGHT = 1;
    private static final Double INITIAL_ACTOR_WEIGHT = 10.0;
    private static final Double INITIAL_CHARACTER_WEIGHT = 5.0;

    public static void main(String[] args) {
        new ImdbActorsParser().crawl();
    }

    public void run(Keyspace keyspace) throws Exception {
        MovieService movieService = new MovieService(keyspace);
        PersonService personService = new PersonService(keyspace);
        TagService tagService = movieService;

        String filePath = PropertyManager.get(PropertyManager.Property.IMDB_ACTORS_FILE);

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        String line = fileReader.readLine();
        Pattern actorMovieCharacterPattern = Pattern.compile("^([a-zA-Z0-9\\s]+),\\s([a-zA-Z0-9\\s]+)\\s\\s(.+)\\((\\d+)\\).*\\[(.+)\\].*$");
        Pattern actorMoviePattern = Pattern.compile("^([a-zA-Z0-9\\s]+),\\s([a-zA-Z0-9\\s]+)\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\])?+$");
        Pattern movieCharacterPattern = Pattern.compile("^\\s\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\]).*$");
        Pattern moviePattern = Pattern.compile("^\\s\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\])?+$");

        String name = null;
        String surname = null;

        String movie = null;
        String year;
        String character;

        InformationItem actorItem = null;
        boolean needUpdateActor;

        int lineNumber = 0;
        int actorsCount = 0;
        TimeWatch watch = TimeWatch.start();

        while (line != null) {
            try {
                if ("".equals(line)) {
                    continue;
                }

                lineNumber++;
                if (lineNumber % 5000 == 0) {
                    long passed = watch.time();
                    watch.reset();
                    System.out.println(String.format("Processed %d lines. Going at speed %.3f lines per second", lineNumber, 5000d * 1000 / passed));
                }

                needUpdateActor = false;

                Matcher actorMovieCharacterMatcher;
                Matcher actorMovieMatcher;
                Matcher movieCharacterMatcher;
                Matcher movieMatcher;
                if ((actorMovieCharacterMatcher = actorMovieCharacterPattern.matcher(line)).matches()) {
                    /* new actor with role and character*/
                    needUpdateActor = true;

                    surname = actorMovieCharacterMatcher.group(1).trim();
                    name = actorMovieCharacterMatcher.group(2).trim();
                    movie = cropMovieName(actorMovieCharacterMatcher.group(3));
                    year = actorMovieCharacterMatcher.group(4).trim();
                    character = actorMovieCharacterMatcher.group(5).trim();
                } else if ((actorMovieMatcher = actorMoviePattern.matcher(line)).matches()) {
                    /* new actor with no character */
                    needUpdateActor = true;

                    surname = actorMovieMatcher.group(1).trim();
                    name = actorMovieMatcher.group(2).trim();
                    movie = cropMovieName(actorMovieMatcher.group(3));
                    year = actorMovieMatcher.group(4).trim();
                    character = null;
                } else if ((movieCharacterMatcher = movieCharacterPattern.matcher(line)).matches()) {
                    /* new role with character */
                    movie = cropMovieName(movieCharacterMatcher.group(1)).trim();
                    year = movieCharacterMatcher.group(2).trim();
                    character = movieCharacterMatcher.group(4).trim();
                } else if ((movieMatcher = moviePattern.matcher(line)).matches()) {
                    /* new role only */
                    movie = cropMovieName(movieMatcher.group(1)).trim();
                    year = movieMatcher.group(2).trim();
                    character = null;
                } else {
                    /* No match was found */
                    continue;
                }

                try {

                    InformationItem movieItem = movieService.getTag(movie); //TODO: names may differ, need to make this thing smarter
                    if (movie == null) {
                        movieItem = movieService.getByNameSimplified(movie);
                    }
//                    Collection<InformationItem> movieItems = movieService.multigetByMeta(MovieService.NAME, movie);
//                    InformationItem movieItem = null;
//                    for (InformationItem movieItemCandidate : movieItems) {
//                        int movieLensYear = Integer.parseInt(movieItemCandidate.getMeta(MovieService.YEAR));
//                        int imdbYear = Integer.parseInt(year);
//                        if (Math.abs(movieLensYear - imdbYear) <= 2) {
//                            movieItem = movieItemCandidate;
//                            break;
//                        }
//                    }

                    if (movieItem != null) {
                        System.out.println("Found one match for " + name + " " + surname + " in " + movie);

                        if (needUpdateActor) {
                            actorItem = personService.createPerson(name, surname);
                            actorsCount++;
                        }

                        movieService.setComponentWeight(movieItem, actorItem, INITIAL_ACTOR_WEIGHT);

                        if (character != null) {
                            InformationItem characterItem = tagService.createTag(character); //TODO: insert check for existence here

                            movieService.setComponentWeight(movieItem, characterItem, INITIAL_CHARACTER_WEIGHT);
                            movieService.setComponentWeight(actorItem, characterItem, INITIAL_CHARACTER_WEIGHT);
                        }
                    }
                } catch (Exception e) {
                    System.out.println(System.currentTimeMillis() + " Failed to add actor " + name + " " + surname + " to movie " + movie);
                    continue;
                } finally {
                    character = null;
                }

            } catch (Exception e) {
                System.out.println("Failed perform match; reason: " + e.getMessage());
                e.printStackTrace();
                continue;
            } finally {
                line = fileReader.readLine();
            }
        }
        System.out.println(String.format("Processed %d actors all-in-all", actorsCount));
    }

    private String cropMovieName(String movieName) {
        String result = movieName.trim();
        if (result.charAt(0) == '"' || result.charAt(result.length() - 1) == '\''){
            result = result.substring(1);
        }

        if (result.charAt(result.length() - 1) == '"' || result.charAt(result.length() - 1) == '\''){
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

}
