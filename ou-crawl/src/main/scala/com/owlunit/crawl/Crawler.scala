package com.owlunit.crawl

import model.PsKeyword
import model.PsMovie
import model.PsPerson
import model.PsRole
import parser.{Parser, PersonsCrawler, KeywordsParser, MoviesParser}
import com.weiglewilczek.slf4s.Logging
import com.owlunit.web.config.{IiDaoConfig, MongoConfig}
import com.owlunit.web.model.{Keyword, Movie}
import org.bson.types.ObjectId

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object Crawler extends Parser with Logging {

  val movies = collection.mutable.Map[String, PsMovie]() // simpleName

  val keywords = collection.mutable.Map[String, ObjectId]()
  val persons = collection.mutable.Map[String, ObjectId]()

  val moviesPath      = "../../raw-data/movielens/movies.dat"
  val keywordsPath    = "../../raw-data/imdb/keywords.list"
  val actorsPath      = "../../raw-data/imdb/actors.list"
  val actressesPath   = "../../raw-data/imdb/actresses.list"
  val directorsPath   = "../../raw-data/imdb/directors.list"
  val producersPath   = "../../raw-data/imdb/producers.list"


  def cacheMovie(movie: PsMovie) = movies += simplifyName(movie.name, movie.year) -> movie

  def saveKeyword(psKeyword: PsKeyword) = {
    val keyword = Keyword.createRecord.name(psKeyword.name).save
    keywords += psKeyword.name -> keyword.id.is
  }

  def saveMovieKeyword(m: PsMovie, k: PsKeyword, w: Double) =
    try {
      val movie = Movie.findBySimpleName(m.name, m.year).open_!
      val keyword = Keyword.find(keywords(k.name)).open_!
      movie.addKeyword(keyword, w)
    } catch {
      case e: Exception => logger.error("Cant increment %s for %s" format (k, m))
    }

  def saveMoviePerson(movie: PsMovie, person: PsPerson, role: PsRole) = {}

  def main(args: Array[String]) {

    MongoConfig.init()
    IiDaoConfig.init()

    MoviesParser.parse(moviesPath, cacheMovie, k => {}, (m, k, w) => {})
//    KeywordsParser.parse(keywordsPath, movies, saveMovieKeyword)

//    PersonsCrawler.parse(actorsPath, movies, PsRole("Actor"), 12338129, saveMoviePerson)
//    PersonsCrawler.parse(actressesPath, movies, PsRole("Actor"), 7243872)
//    PersonsCrawler.parse(directorsPath, movies, PsRole("Director"), 1724653)
//    PersonsCrawler.parse(producersPath, movies, PsRole("Producer"), 3719561)


    val counter = Counter.start(10679)
    for (movie <- movies.values) {
      counter.tick(logger, 1000, "movies saved")
      Movie.createRecord.name(movie.name).year(movie.year).save
    }

  }

}