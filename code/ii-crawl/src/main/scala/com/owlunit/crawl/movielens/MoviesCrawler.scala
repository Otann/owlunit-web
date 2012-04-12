package com.owlunit.crawl.movielens

import com.codahale.logula.Logging
import io.Source
import com.owlunit.crawl.Counter
import com.owlunit.service.cinema.{CinemaService, MovieIi, MovieService, KeywordService}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class MoviesCrawler(sourcePath: String, cinemaService: CinemaService) extends Logging {

  val Pattern = """(\d+)::([^\(]+) \((\d+)\)::(.*)""".r

  def run() {

    val timer = Counter.start(10681)

    log.debug("Strating to crawl movies from MovieLens")
    for (line <- Source.fromFile(sourcePath).getLines(); if line != "") line match {

      case Pattern(id, name, year, genresRaw) => {
        val genres = genresRaw.split('|').toSet.map((g: String) => cinemaService.loadOrCreateKeyword(g))
        val movie = new MovieIi(0, name, year.toInt, tags = genres)
        cinemaService.createMovie(movie)

        timer.tick(log, 500, "movies");
      }
      case unmatched => {
        print("String was not matched: " + unmatched)
      }

    }
  }

}