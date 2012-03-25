package com.owlunit.crawl.movielens

import com.codahale.logula.Logging
import io.Source
import scala.util.control.Breaks._
import util.matching.Regex
import java.util.regex.Pattern
import com.owlunit.core.ii.IiDao
import com.owlunit.service.cinema.{Keyword, KeywordService, Movie, MovieService}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MovieLensMoviesCrawler {

  def main(args: Array[String]) {
    if (args.length < 2) {
      print("Insuficient args, provide db and maser file pathes")
    } else {
      val dao = IiDao(args(1));
      new MovieLensMoviesCrawler(
        args(0),
        new MovieService(dao),
        new KeywordService(dao));
    }
  }

}

class MovieLensMoviesCrawler( path: String,
                              movieService: MovieService,
                              keywordService: KeywordService) extends  Logging {

  val pattern = Pattern.compile("""(\d+)::(.+) \((\d+)\)::(.*)""")

  def run() {


    for {line <- Source.fromFile(path).getLines()
         if line != ""
         m = pattern.matcher(line)
         if m.matches()
    } {

      val name = m.group(2).substring(0, m.group(2).indexOf("("));
      val year = m.group(3).toInt;
      val genres = m.group(4).split('|').map(keywordService.loadOrCreate(_)).toSet;

      val movie = new Movie(0, name, year, tags = genres)
      movieService.create(movie)

    }
  }

}