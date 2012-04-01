package com.owlunit.crawl.movielens

import com.codahale.logula.Logging
import io.Source
import com.owlunit.crawl.Counter
import com.owlunit.service.cinema.{Movie, MovieService, KeywordService}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class MoviesCrawler( sourcePath: String,
                     movieService: MovieService,
                     keywordService: KeywordService) extends  Logging {

  val Pattern = """(\d+)::(.+) \((\d+)\)::(.*)""".r

  def run() {

    val timer = Counter.start(10681)

    log.debug("Strating to crawl movies from MovieLens")
    for (line <- Source.fromFile(sourcePath).getLines(); if line != "") line match {

      case Pattern(id, name, year, genresRaw) => {
        val genres = genresRaw.split('|').toSet.map((g: String) => keywordService.loadOrCreate(g))
        val movie = new Movie(0, name, year.toInt, tags = genres)
        movieService.create(movie)

        timer.tick(log, 500, "movies");
      }
      case unmatched => {
        print("String was not matched: " + unmatched)
      }

    }
  }

}