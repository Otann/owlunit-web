//package com.owlunit.crawlers.movielens;
//
//import com.owlunit.crawlers.common.TimeWatch;
//import com.owlunit.service.cinema.KeywordServiceImpl;
//import com.owlunit.service.cinema.MovieServiceImpl;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.context.support.ClassPathXmlApplicationContext;
//
//import java.io.BufferedReader;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
///**
// * Many Monkeys
// *
// * @author Anton Chebotaev
// * @author Ilya Pimenov
// */
//public class MoviesCrawler {
//
//    final Logger log = LoggerFactory.getLogger(MoviesCrawler.class);
//
//    ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
//    MovieServiceImpl movieService = (MovieServiceImpl) ctx.getBean("movieService");
//    KeywordServiceImpl keywordService = (KeywordServiceImpl) ctx.getBean("keywordService");
//
//    public static final String SERVICE_NAME = "movielens";
//    private static final String A_K_A = "a.k.a.";
//
//    public static void main(String[] args) throws IOException {
//        new MoviesCrawler().run(args[0]);
//    }
//
//    public void run(String filePath) throws IOException {
//
//        BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "UTF8"));
//
//        String line = fileReader.readLine();
//
//        TimeWatch watch = TimeWatch.start();
//
//        while (line != null) {
//
//            if ("".equals(line)) {
//                continue;
//            }
//
//            int fistSemicolon = line.indexOf(':');
//            String id = line.substring(0, fistSemicolon);
//            int lastSemicolon = line.lastIndexOf(':');
//            String name = line.substring(fistSemicolon + 2, lastSemicolon - 2 - 6).trim();
//            String nameTranslate = null;
//            String aka = null;
//            if (name.charAt(name.length() - 1) == ')') {
//                nameTranslate = name.substring(name.indexOf("(") + 1, name.length() - 1);
//                name = name.substring(0, name.indexOf("(")).trim();
//                if (nameTranslate.startsWith(A_K_A)) {
//                    aka = nameTranslate.substring(A_K_A.length(), nameTranslate.length()).trim();
//                    nameTranslate = null;
//                }
//            }
//            String year = line.substring(lastSemicolon - 6, lastSemicolon - 2);
//            String[] genres = line.substring(lastSemicolon + 1, line.length()).split("\\|");
//
//            MovieIi movie = movieService.createMovie(new MovieIi(0, name, Long.parseLong(year), null));
//            try {
//                movieService.setExternalId(movie, SERVICE_NAME, id);
//                if (aka != null) {
//                    movieService.setAkaName(movie, aka);
//                }
//                if (nameTranslate != null) {
//                    movieService.setTranslateName(movie, nameTranslate);
//                }
//                for (String genre : genres) {
//                    KeywordIi tag = keywordService.loadOrCreateKeyword(genre);
//                    movieService.addKeyword(movie, tag);
//                }
//            } catch (NotFoundException e) {
//                log.error("Can't find object" + movie.toString());
//            }
//
//            watch.tick(log, 100, "Crawling movielens.", "movies");
//            line = fileReader.readLine();
//
//        }
//        log.info("All done");
//
//        ctx.close();
//    }
//
//}
