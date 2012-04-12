//package com.owlunit.crawlers.movielens;
//
//import com.owlunit.crawlers.common.TimeWatch;
//import com.owlunit.model.cinema.KeywordIi;
//import com.owlunit.model.cinema.MovieIi;
//import com.owlunit.orthodoxal.service.cinema.MovieServiceImpl;
//import com.owlunit.orthodoxal.service.exception.NotFoundException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import java.io.*;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Many Monkeys
// *
// * @author Ilya Pimenov
// */
//public class MovieLensTagsCrawler {
//
//    public static final String EMPTY_STRING = "";
//
//    final Logger log = LoggerFactory.getLogger(MovieLensTagsCrawler.class);
//
//    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//    MovieServiceImpl movieService = (MovieServiceImpl) ctx.getBean("movieService");
//    KeywordServiceImpl keywordService = (KeywordServiceImpl) ctx.getBean("keywordService");
//
//    public static void main(String[] args) throws IOException {
//        new MovieLensTagsCrawler().run(args[0]);
//    }
//
//    public void run(String filePath) throws IOException {
//
//        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
//
//        Map<String, KeywordIi> keywordLocalCache = new HashMap<String, KeywordIi>();
//        Map<String, MovieIi> movieLocalCache = new HashMap<String, MovieIi>();
//
//        TimeWatch watch = TimeWatch.start();
//
//        String line = fileReader.readLine();
//        Pattern p = Pattern.compile("\\:\\:(.*)\\:\\:");
//        while (line != null) {
//            if (EMPTY_STRING.equals(line))
//                continue;
//
//            try {
//                Matcher matcher = p.matcher(line);
//                matcher.find();
//                String str = matcher.group(1);
//                String externalId = str.substring(0, str.indexOf(':'));
//                String tagName = str.substring(str.lastIndexOf(':') + 1, str.length()).toLowerCase();
//
//                MovieIi movie = movieLocalCache.get(externalId);
//                if (movie == null) {
//                    try {
//                        movie = movieService.loadByExternalId(MoviesCrawler.SERVICE_NAME, externalId);
//                    } catch (NotFoundException e) {
//                        movie = null;
//                    }
//                    movieLocalCache.put(externalId, movie);
//                }
//
//                watch.tick(log, 2000, "Processing movielens.", "tags");
//
//                KeywordIi keyword = keywordLocalCache.get(tagName);
//                if (keyword == null) {
//                    keyword = keywordService.createKeyword(tagName);
//                    keywordLocalCache.put(tagName, keyword);
//                }
//
//                // Todo Ilya Pimenov this can be very slow, cuz it requires DB request. Better to keep it cached.
//                // Or kill this parses completely, MovieLens tags are crap
//                if (movieService.hasKeyword(movie, keyword)) {
//                    movieService.addKeyword(movie, keyword);
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
//
//        }
//        log.info("All done.");
//        ctx.close();
//    }
//
//}
