//package com.owlunit.crawlers.imdb;
//
//import com.owlunit.crawlers.common.CassandraCrawler;
//import com.owlunit.crawlers.common.TimeWatch;
//import com.owlunit.model.cinema.Movie;
//import com.owlunit.model.cinema.Person;
//import com.owlunit.orthodoxal.service.cinema.MovieServiceImpl;
//import com.owlunit.orthodoxal.service.cinema.PersonServiceImpl;
//import com.owlunit.orthodoxal.service.exception.NotFoundException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Rocket Science Software
// *
// * @author Ilya Pimenov
// * @author Anton Chebotaev
// */
//public class ImdbPersonCrawler extends CassandraCrawler {
//
//    public static final String DEFAULT_EMPTY_NAME = "";
//
//    final Logger log = LoggerFactory.getLogger(ImdbPersonCrawler.class);
//
//    private final String filePath;
//    private final String role;
//
//    MovieServiceImpl movieService = (MovieServiceImpl) ctx.getBean("movieService");
//    PersonServiceImpl personService = (PersonServiceImpl) ctx.getBean("personService");
//
//    Pattern PERSON_MOVIE_PATTERN = Pattern.compile("^([^\\t]+)\\t+(.+)\\((\\d+)\\).*$");
//    Pattern MOVIE_PATTERN = Pattern.compile("^\\t\\t\\t(.+)\\((\\d+)\\).*$");
//
//    int actorsCount = 0;
//
//    public ImdbPersonCrawler(String filePath, String role) {
//        this.filePath = filePath;
//        this.role = role;
//    }
//
//    public static void main(String[] args) {
//        new ImdbPersonCrawler(args[0], args[1]).crawl();
//    }
//
//    public void run() throws Exception {
//
//        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));
//        String line = fileReader.readLine();
//
//        String personName = null;
//        String movieName;
//        long year;
//
//        List<Movie> movies = new ArrayList<Movie>();
//
//        TimeWatch timer = TimeWatch.start();
//
//        while (line != null) {
//            try {
//                timer.tick(log, 10000, String.format("Found %d persons in file %s.", actorsCount, filePath), "lines");
//
//                if ("".equals(line)) {
//                    continue;
//                }
//
//                Matcher personMovieMatcher;
//                Matcher movieMatcher;
//
//                if ((personMovieMatcher = PERSON_MOVIE_PATTERN.matcher(line)).matches()) {
//                    // Means we found new person, so store everything for old person
//                    flushMoviesForPerson(personName, movies);
//                    if (!movies.isEmpty()) {
//                        movies = new LinkedList<Movie>();
//                    }
//
//                    personName = personMovieMatcher.group(1).trim();
//                    movieName = cropMovieName(personMovieMatcher.group(2));
//                    year = Long.parseLong(personMovieMatcher.group(3));
//                } else if ((movieMatcher = MOVIE_PATTERN.matcher(line)).matches()) {
//                    movieName = cropMovieName(movieMatcher.group(1)).trim();
//                    year = Long.parseLong(movieMatcher.group(2));
//                } else {
//                    continue;
//                }
//
//                try {
//                    Movie movie = movieService.loadByName(movieName, year);
//                    movies.add(movie);
//                } catch (NotFoundException e) {
//                    //noinspection UnnecessaryContinue
//                    continue;
//                }
//
//            } catch (Exception e) {
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                e.printStackTrace(pw);
//                log.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
//            } finally {
//                line = fileReader.readLine();
//            }
//        }
//
//        log.info(String.format("Processed %d persons all-in-all.", actorsCount));
//    }
//
//    private void flushMoviesForPerson(String personName, Collection<Movie> movies) throws NotFoundException {
//        if (personName != null && movies != null && !movies.isEmpty()) {
//            String[] fullName = splitName(personName);
//            Person person = personService.findOrCreate(new Person(0, fullName[0], fullName[1], null));
//            if (!person.getRoles().contains(Role.valueOf(role))) {
//                personService.addRole(person, Role.valueOf(role));
//            }
//            actorsCount++;
//            for (Movie movie : movies) {
//                movieService.addPerson(movie, person, Role.valueOf(role));
//            }
//        }
//    }
//
//    private String[] splitName(String fullname) {
//        String name;
//        String surname;
//        if (fullname.indexOf(", ") > 0) {
//            String[] parts = fullname.split(", ");
//            name = parts[1];
//            surname = parts[0];
//        } else {
//            name = DEFAULT_EMPTY_NAME;
//            surname = name;
//        }
//        return new String[]{name, surname};
//    }
//
//    private String cropMovieName(String movieName) {
//        String result = movieName.trim();
//        if (result.charAt(0) == '"' || result.charAt(result.length() - 1) == '\'')
//            result = result.substring(1);
//
//        if (result.charAt(result.length() - 1) == '"' || result.charAt(result.length() - 1) == '\'')
//            result = result.substring(0, result.length() - 1);
//
//        return result;
//    }
//
//}
