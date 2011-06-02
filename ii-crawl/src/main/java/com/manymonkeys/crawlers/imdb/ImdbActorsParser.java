
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rocket Science Software
 *
 * @author Ilya Pimenov
 */
public class ImdbActorsParser extends CassandraCrawler {

    private static long DEFAULT_WEIGHT = 1;

    private static final Double INITIAL_ACTOR_WEIGHT = 5.0;
    private static final Double INITIAL_CHARACTER_WEIGHT = 2.5;
    private static final Double ADDITIONAL_CHARACTER_WEIGHT = 2.5;

    private Map<String, UUID> localCharactersCache = new HashMap<String, UUID>();

    public static void main(String[] args) {
        new ImdbActorsParser().crawl();
    }

    @SuppressWarnings({"UnusedAssignment"})
    public void run(Keyspace keyspace) throws Exception {
        MovieService movieService = new MovieService(keyspace);
        PersonService personService = new PersonService(keyspace);

        String filePath = PropertyManager.get(PropertyManager.Property.IMDB_ACTORS_FILE);

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        String line = fileReader.readLine();
        Pattern actorMovieCharacterPattern = Pattern.compile("^([a-zA-Z0-9$'\\s]+),\\s([a-zA-Z0-9\\s]+)\\s\\s(.+)\\((\\d+)\\).*\\[(.+)\\].*$");
        Pattern actorMoviePattern = Pattern.compile("^([a-zA-Z0-9\\s]+),\\s([a-zA-Z0-9\\s]+)\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\])?+$");
        Pattern movieCharacterPattern = Pattern.compile("^\\s\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\]).*$");
        Pattern moviePattern = Pattern.compile("^\\s\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\])?+$");

        // Init strings
        String name = null;
        String surname = null;
        String movie;

        String oldName = null;
        String oldSurname = null;
        String oldMovie = null;

        String year = null;
        String character = null;

        InformationItem actorItem = null;
        String lastActorName = null;
        String lastActorSurname = null;

        int lineNumber = 0;
        int actorsCount = 0;
        TimeWatch watch = TimeWatch.start();

        while (line != null) {
            try {
                if ("".equals(line)) {
                    continue;
                }

                lineNumber++;
                if (lineNumber % 10000 == 0) {
                    long passed = watch.time();
                    watch.reset();
                    System.out.println(String.format("Processed %d lines. Going at speed %.3f lines per second. Found %d actors", lineNumber, 10000d * 1000 / passed, actorsCount));
                }

                Matcher actorMovieCharacterMatcher;
                Matcher actorMovieMatcher;
                Matcher movieCharacterMatcher;
                Matcher movieMatcher;

                if ((actorMovieCharacterMatcher = actorMovieCharacterPattern.matcher(line)).matches()) {
                    /* new actor with role and character*/
                    surname = actorMovieCharacterMatcher.group(1).trim();
                    name = actorMovieCharacterMatcher.group(2).trim();
                    actorItem = null;

                    movie = cropMovieName(actorMovieCharacterMatcher.group(3));
                    year = actorMovieCharacterMatcher.group(4).trim();
                    character = actorMovieCharacterMatcher.group(5).trim();
                } else if ((actorMovieMatcher = actorMoviePattern.matcher(line)).matches()) {
                    /* new actor with no character */
                    surname = actorMovieMatcher.group(1).trim();
                    name = actorMovieMatcher.group(2).trim();
                    actorItem = null;

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

                // This occurs on lines like and we do not distinguish these situations for now
                // "Dungeons & Dragons" (1983) {Citadel of Shadow (#3.4)}  (voice)  [Hank the Ranger]  <1>
                // "Dungeons & Dragons" (1983) {OTHER PART OF THAT MOVIE}  (voice)  [Hank the Ranger]  <1>
                if (movie.equals(oldMovie) && name.equals(oldName) && surname.equals(oldSurname)) {
                    continue;
                } else {
                    oldMovie = movie;
                    oldName = name;
                    oldSurname = surname;
                }

                try {

                    InformationItem movieItem = movieService.getByNameSimplified(movie); //TODO: names may differ, need to make this thing smarter
                    if (movieItem == null) {
                        continue;
                    }

                    if (actorItem == null) {
                        // Means new actor parsed and we need new Item
                        // We don't check for existence here because actors go straight-forward in IMDB file
                        actorItem = personService.createPerson(name, surname);
                        actorsCount++;
                    }

                    movieService.setComponentWeight(movieItem, actorItem, INITIAL_ACTOR_WEIGHT);

/*                    if (character != null) {
                        InformationItem characterItem = null;

                        if (localCharactersCache.containsKey(character)) {
                            characterItem = personService.getByUUID(localCharactersCache.get(character));
                            double oldWeight = actorItem.getComponentWeight(characterItem);
                            movieService.setComponentWeight(actorItem, characterItem, oldWeight + ADDITIONAL_CHARACTER_WEIGHT);
                        } else {
                            characterItem = personService.createTag(character);
                            localCharactersCache.put(character, characterItem.getUUID());
                            movieService.setComponentWeight(actorItem, characterItem, INITIAL_CHARACTER_WEIGHT);
                        }

                        // Character-Movie combination never repeated, so use initial weight
                        movieService.setComponentWeight(movieItem, characterItem, INITIAL_CHARACTER_WEIGHT);
                    }
*/

                } catch (Exception e) {
                    System.out.println(System.currentTimeMillis() + " Failed to add actor " + name + " " + surname + " to movie " + movie);
                    e.printStackTrace();
                }

            } catch (Exception e) {
                System.out.println("Failed perform match; reason: " + e.getMessage());
                e.printStackTrace();
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
