//package com.owlunit.crawlers.imdb;
//
//import com.owlunit.crawlers.common.CassandraCrawler;
//import com.owlunit.crawlers.common.TimeWatch;
//import com.owlunit.model.cinema.MovieIi;
//import com.owlunit.orthodoxal.service.cinema.MovieServiceImpl;
//import com.owlunit.orthodoxal.service.exception.NotFoundException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Many Monkeys
// *
// * @author Anton Chebotaev
// */
//public class ImdbPlotCrawler extends CassandraCrawler {
//
//    final Logger log = LoggerFactory.getLogger(ImdbPlotCrawler.class);
//
//    MovieServiceImpl movieService = (MovieServiceImpl) ctx.getBean("movieService");
//
//    static final Pattern MOVIE_NAME = Pattern.compile("^MV: (.+) \\((\\d+)\\).*$");
//    static final Pattern PLOT_LINE = Pattern.compile("PL: (.+)$");
//
//    private String filePath;
//
//    public ImdbPlotCrawler(String filePath) {
//        this.filePath = filePath;
//    }
//
//    public static void main(String[] args) {
//        new ImdbPlotCrawler(args[0]).crawl();
//    }
//
//    @Override
//    public void run() throws Exception {
//
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), "windows-1250"));
//
//        parseMovies(reader);
//    }
//
//    private void parseMovies(BufferedReader reader) throws IOException {
//        TimeWatch watch = TimeWatch.start();
//
//        String line = reader.readLine();
//        MovieIi movieItem = null;
//        StringBuffer buffer = new StringBuffer();
//
//        while (line != null) {
//            try {
//
//                watch.tick(log, 100000, "Processing plots.", "lines");
//
//                Matcher matcher;
//                if ((matcher = MOVIE_NAME.matcher(line)).matches()) {
//                    if (movieItem != null) {
//                        movieService.setDescription(movieItem, buffer.toString());
//                        buffer = new StringBuffer();
//                    }
//
//                    String movieName = matcher.group(1);
//                    long year = Long.parseLong(matcher.group(2));
//                    try {
//                        movieItem = movieService.loadByName(movieName, year);
//                    } catch (NotFoundException e) {
//                        movieItem = null;
//                    }
//                } else if ((matcher = PLOT_LINE.matcher(line)).matches()) {
//                    if (movieItem == null)
//                        continue;
//
//                    buffer.append(matcher.group(1));
//                    buffer.append(' ');
//                } else if ((matcher = PLOT_LINE.matcher(line)).matches()) {
//                    if (movieItem == null)
//                        continue;
//
//                    buffer.append("author :");
//                    buffer.append(matcher.group(1));
//                    buffer.append('\n');
//                }
//
//            } catch (Exception e) {
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                e.printStackTrace(pw);
//                log.error(String.format("Unable to parse line %s. Exception: %s", line, sw.toString()));
//            } finally {
//                line = reader.readLine();
//            }
//        }
//
//        if (movieItem != null) {
//            try {
//                movieService.setDescription(movieItem, buffer.toString());
//            } catch (NotFoundException e) {
//                log.error(String.format("Loaded object can't be updated, %s", movieItem.toString()));
//            }
//        }
//    }
//
//}
