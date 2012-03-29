package com.owlunit.crawl.imdb

import com.codahale.logula.Logging
import io.Source
import collection.mutable.{Map => MutableMap}
import com.owlunit.service.cinema.impl.{KeywordServiceImpl, MovieServiceImpl}
import com.owlunit.service.cinema.{KeywordServiceImpl, MovieServiceImpl}
import com.owlunit.crawl.Counter

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class KeywordsCrawler( sourcePath: String,
                       movieService: MovieServiceImpl,
                       keywordService: KeywordServiceImpl) extends  Logging {

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
    log.debug("Reading all keywords")
    val frequencies = MutableMap[String, Int]()
    try {
    while (source.hasNext && !line.contains("THE KEYWORDS LIST")) {
      for (keywordExtractor(name, amount) <- keywordsExtractor.findAllIn(line)) {
        frequencies += name -> amount.toInt
      }
      line = source.next()
    }
    } catch {
      case ex: Exception => log.error("Catched unhandeled exception %s for line %s" format (ex, line))
    }

    // read movies
    log.debug("Reading movie-keyword pairs")
    val timer = Counter.start()
    val linesTimer = Counter.start(4113524)
    while (source.hasNext) {
      val line = source.next()
      linesTimer.tick(log, 100000, "lines so far")
      try {

        val keywordLine(name, yearRaw, keywordRaw) = line
        val movie = movieService.load(name, yearRaw.toInt)

        if (movie.isDefined) {
          val keyword = keywordService.loadOrCreate(capitalizeKeyword(keywordRaw))
          movieService.addKeyword(movie.get, keyword, frequencies(keywordRaw))
          timer.tick(log, 50000, "movie-keyword relations")
        }
      } catch {
        case ex: MatchError => // log.trace("Error matching line " + line)
        case ex: Exception => log.error("Catched unhandeled exception %s for line %s" format (ex, line))
      }

    }

  }


}