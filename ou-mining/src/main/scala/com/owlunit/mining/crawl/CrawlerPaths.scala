package com.owlunit.crawl

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
trait CrawlerPaths {

  val moviesPath      = "../../data-raw/movielens/movies.dat"
  val keywordsPath    = "../../data-raw/imdb/keywords.list"
  val actorsPath      = "../../data-raw/imdb/actors.list"
  val actressesPath   = "../../data-raw/imdb/actresses.list"
  val directorsPath   = "../../data-raw/imdb/directors.list"
  val producersPath   = "../../data-raw/imdb/producers.list"

  val parsedMovies    = "../../data-raw/parsed/movies.case"
  val parsedKeywords  = "../../data-raw/parsed/keywords.case"

}
