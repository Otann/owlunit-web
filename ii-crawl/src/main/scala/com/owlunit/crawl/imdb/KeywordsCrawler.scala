package com.owlunit.crawl.imdb

import io.Source
import collection.mutable.{Map => MutableMap}
import com.owlunit.crawl.Counter
import com.owlunit.service.cinema.CinemaService
import com.weiglewilczek.slf4s.Logging

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class KeywordsCrawler(sourcePath: String, cinemaService: CinemaService) extends Logging {

  val keywordsExtractor = """([^\s]+ \(\d+\))""".r
  val keywordExtractor = """([^\s]+) \((\d+)\)""".r
  val keywordLine = """^(.*) \((\d+)\).*\t([^\s]*)\s*""".r

  def capitalizeWord(s: String) = { s(0).toUpper + s.substring(1, s.length).toLowerCase }
  def capitalizeKeyword(k: String) = k.split('-').map(capitalizeWord).mkString(" ")
  
  def run() {

    val source = Source.fromFile(sourcePath).getLines()

    // skip first part
    var line = source.next()
    while (!line.contains("keywords in use"))
      line = source.next()

    // read all keywords
    logger.debug("Reading all keywords")
    val frequencies = MutableMap[String, Int]()
    try {
    while (source.hasNext && !line.contains("THE KEYWORDS LIST")) {
      for (keywordExtractor(name, amount) <- keywordsExtractor.findAllIn(line)) {
        frequencies += name -> amount.toInt
      }
      line = source.next()
    }
    } catch {
      case ex: Exception => logger.error("Catched unhandeled exception %s for line %s" format (ex, line))
    }

    // read movies
    logger.debug("Reading movie-keyword pairs")
    val timer = Counter.start()
    val linesTimer = Counter.start(4113524)
    while (source.hasNext) {
      val line = source.next()
      linesTimer.tick(logger, 100000, "lines so far")
      try {

        val keywordLine(name, yearRaw, keywordRaw) = line
        val movie = cinemaService.loadMovie(name, yearRaw.toInt)

        if (movie.isDefined) {
          val keyword = cinemaService.loadOrCreateKeyword(capitalizeKeyword(keywordRaw))
          cinemaService.addKeyword(movie.get, keyword, frequencies(keywordRaw))
          timer.tick(logger, 50000, "movie-keyword relations")
        }
      } catch {
        case ex: MatchError => // log.trace("Error matching line " + line)
        case ex: Exception => logger.error("Catched unhandeled exception %s for line %s" format (ex, line))
      }

    }

  }

}