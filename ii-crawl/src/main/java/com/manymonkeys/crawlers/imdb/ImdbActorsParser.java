package com.manymonkeys.crawlers.imdb;

import com.manymonkeys.core.ii.InformationItem;
import com.manymonkeys.crawlers.common.CassandraCrawler;
import com.manymonkeys.crawlers.common.PropertyManager;
import com.manymonkeys.service.cinema.MovieService;
import com.manymonkeys.service.cinema.TagService;
import me.prettyprint.hector.api.Keyspace;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rocket Science Software
 *
 * @author Ilya Pimenov
 */
public class ImdbActorsParser extends CassandraCrawler {

    private static long DEFAULT_WEIGHT = 1;

    public static void main(String[] args) {
        new ImdbActorsParser().crawl();
    }

    public void run(Keyspace keyspace) throws Exception {
        MovieService movieService = new MovieService(keyspace);
        TagService tagService = new TagService(keyspace);

        String filePath = PropertyManager.get(PropertyManager.Property.IMDB_ACTORS_FILE);

        BufferedReader fileReader = new BufferedReader(new FileReader(filePath));

        String line = fileReader.readLine();
        Pattern actorMovieCharacterPattern = Pattern.compile("^([a-zA-Z0-9\\s]+),\\s([a-zA-Z0-9\\s]+)\\s\\s(.+)\\((\\d+)\\).*\\[(.+)\\].*$");
        Pattern actorMoviePattern = Pattern.compile("^([a-zA-Z0-9\\s]+),\\s([a-zA-Z0-9\\s]+)\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\])?+$");
        Pattern moviePattern = Pattern.compile("^\\s\\s\\s(.+)\\((\\d+)\\).*(\\[(.+)\\])?+$");

        String name = null;
        String surname = null;

        String movie = null;
        String year;
        String character;
        while (line != null) {
            try {
                if ("".equals(line)) {
                    continue;
                }

                Matcher actorMovieCharacterMatcher;
                Matcher actorMovieMatcher;
                Matcher movieMatcher;
                if ((actorMovieCharacterMatcher = actorMovieCharacterPattern.matcher(line)).matches()) {
                    surname = actorMovieCharacterMatcher.group(1).trim();
                    name = actorMovieCharacterMatcher.group(2).trim();
                    movie = cropMovieName(actorMovieCharacterMatcher.group(3));
                    year = actorMovieCharacterMatcher.group(4).trim();
                    character = actorMovieCharacterMatcher.group(5).trim();
//                    System.out.println(name + " " + surname + " played in " + movie + " " + year + " character " + character);
                } else if ((actorMovieMatcher = actorMoviePattern.matcher(line)).matches()) {
                    surname = actorMovieMatcher.group(1).trim();
                    name = actorMovieMatcher.group(2).trim();
                    movie = cropMovieName(actorMovieMatcher.group(3));
                    year = actorMovieMatcher.group(4).trim();
//                    System.out.println(name + " " + surname + " played in " + movie + " " + year);
                } else if ((movieMatcher = moviePattern.matcher(line)).matches()) {
                    movie = cropMovieName(movieMatcher.group(1)).trim();
                    year = movieMatcher.group(2).trim();
//                    System.out.println(name + " " + surname + " played in " + movie + " " + year);
                }
                /* No match was found */
//                System.out.println(line);

                try {
                    InformationItem movieTag = movieService.getTag(movie);
                    if (movieTag != null) {
                        System.out.println("Found one match for " + name + " " + surname + " in " + movie);
                        //TODO Ilya Pimenov : need to check Year aswell
                        //TODO Ilya Pimenov : Store to Cassandra here !
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
