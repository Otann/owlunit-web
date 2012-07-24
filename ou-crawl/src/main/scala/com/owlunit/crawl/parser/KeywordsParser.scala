package com.owlunit.crawl.parser

import io.Source
import collection.mutable.{Map => MutableMap}
import com.owlunit.crawl._
import com.weiglewilczek.slf4s.Logging
import com.owlunit.web.model.Movie
import model.{PsMovie, PsWeights, PsKeyword}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object KeywordsParser extends Parser with PsWeights with Logging {

  val keywordsExtractor = """([^\s]+ \(\d+\))""".r
  val keywordExtractor = """([^\s]+) \((\d+)\)""".r
  val keywordLine = """^(.*) \((\d+)\).*\t([^\s]*)\s*""".r

  def parse(
             path: String,
             movies: collection.mutable.Map[String, PsMovie],
             flush: (PsMovie, PsKeyword, Double) => Any) {

    val source = Source.fromFile(path).getLines()

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
      case ex: Exception => logger.error("Catched unhandeled exception %s for line %s" format(ex, line))
    }

    val frequencyMin = frequencies.values.min
    val frequencyGap = frequencies.values.max - frequencies.values.min
    def adaptiveFreq(key: String) = (frequencies(key).toDouble - frequencyMin) / frequencyGap
    def adaptiveWeight(key: String) = keywordMinWeight + adaptiveFreq(key) * (keywordMaxWeight - keywordMinWeight)

    // read movies
    logger.debug("Reading movie-keyword pairs")

    val keywordsCounter = Counter.start()
    val linesTimer = Counter.start(4113524)
    while (source.hasNext) {
      val line = source.next()
      linesTimer.tick(logger, 100000, "lines is parsed so far")
      try {

        val keywordLine(name, yearRaw, keywordRaw) = line
        val movie = movies.get(simplifyName(name, yearRaw.toInt))

        if (movie.isDefined) {
          val keyword = PsKeyword(capitalizeKeyword(keywordRaw))
          flush(movie.get, keyword, adaptiveWeight(keywordRaw))
          keywordsCounter.tick(logger, 50000, "movie-keyword relations parsed")
        }

      } catch {
        case ex: MatchError => // log.trace("Error matching line " + line)
        case ex: Exception => logger.error("Catched unhandeled exception %s for line %s" format(ex, line))
      }

    }

  }

}