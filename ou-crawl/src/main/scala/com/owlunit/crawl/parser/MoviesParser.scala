package com.owlunit.crawl.parser

import io.Source
import com.weiglewilczek.slf4s.Logging
import com.owlunit.crawl._
import bootstrap.liftweb.Boot
import model.{PsWeights, PsKeyword, PsMovie}
import net.liftweb.common.Full
import net.liftweb.util.Helpers._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MoviesParser extends Parser with PsWeights with Logging {

  // 4902::Devil's Backbone, The (El Espinazo del diablo) (2001)::Drama|Fantasy|Horror|Thriller|War
  //                id     name    skip opt name      year     genres
  val Pattern = """(\d+)::([^\(]+)(?: \([^\(]*\) )?\((\d+)\)::(.*)""".r

  def parse(
             path: String,
             flushMovie: (PsMovie) => Any,
             flushKeyword: (PsKeyword) => Any,
             flushPair: (PsMovie, PsKeyword, Double) => Any
  ) {

    val counter = Counter.start(10681)

    logger.debug("Strating to crawl movies from MovieLens")

    for (line <- Source.fromFile(path).getLines(); if line != "") line match {
      case Pattern(id, name, yearRaw, genresRaw) => {
        val genres = genresRaw.split('|').toSet.map((g: String) => PsKeyword(capitalizeKeyword(g)))
        val year = yearRaw.toInt // comes as safe Int from regex

        val movie = PsMovie(name.trim, year) //TODO(Anton) fix and check regex

        flushMovie(movie)
        genres.foreach(flushKeyword)
        genres.foreach(k => flushPair(movie, k, keywordMaxWeight))

        counter.tick(logger, 1000, "movies parsed")
      }
      case unmatched => {
        logger.trace("String was not matched: " + unmatched)
      }
    }

    logger.debug("Processed %d movies" format counter.counter)
  }

}