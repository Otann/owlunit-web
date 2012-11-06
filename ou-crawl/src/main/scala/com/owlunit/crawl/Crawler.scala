package com.owlunit.crawl

import com.weiglewilczek.slf4s.Logging
import org.bson.types.ObjectId
import net.liftweb.common.Full
import net.liftweb.util.Props
import com.owlunit.web.config.{IiDaoConfig, MongoConfig}
import com.owlunit.web.model.{Role, Person, Keyword, Movie}

import lib.Counter
import model._
import parser.ParserHelper
import parser.movielens.MoviesParser
import parser.imdb.{PersonsCrawler, KeywordsParser}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object Crawler extends ParserHelper with CrawlerPaths with Logging {

  val movies = collection.mutable.Map[String, PlainMovie]()    // simpleName -> PlainMovie
  val keywords = collection.mutable.Map[String, ObjectId]()
  val persons = collection.mutable.Map[String, ObjectId]()

  def cacheMovie(movie: PlainMovie) = movies += simplifyName(movie.name, movie.year) -> movie

  def saveMovieKeyword(m: PlainMovie, k: PlainKeyword, w: Double) {
    try {
      Movie.findBySimpleName(m.name, m.year) match {

        // Movie found
        case Full(movie) => {

          // Load or create keyword
          val keyword = Keyword.findByName(k.name) match {
            case Full(kw) => kw
            case _ => Keyword.createRecord.nameField(k.name).save
          }

          // Save relation
          movie.addKeyword(keyword).save
        }

        // Movie not found, log error
        case _ => logger.error("Cant find cached movie %s" format m)

      }
    } catch {
      // Don't throw anything out
      case e: Exception => logger.error("Cant increment %s for %s" format (k, m))
    }
  }

  def savePerson(p: PlainPerson) = Person.createRecord.firstName(p.firstName).lastName(p.lastName).save

  def saveMoviePerson(m: PlainMovie, person: Person, role: Role.Role) {
    try {
      Movie.findBySimpleName(m.name, m.year) match {

        // Movie found
        case Full(movie) => movie.addPerson(person, role).save

        // Movie not found, log error
        case _ => logger.error("Cant find cached movie %s" format m)

      }
    } catch {
      // Don't throw anything out, only log
      case e: Exception => logger.error("Cant increment %s for %s" format (person.fullName, m))
    }
  }

  def main(args: Array[String]) {

    logger.info(Props.get("owlunit.neo4j.path", "Path undefined"))

    MongoConfig.init()
    IiDaoConfig.init()

    // Load movies list
    MoviesParser.parse(moviesPath, cacheMovie, k => {}, (m, k) => {})

    // Persist movies
    val counter = Counter.start(10679)
    for (movie <- movies.values) {
      counter.tick(logger, 1000, "movies saved")
      Movie.createRecord.nameField(movie.name).yearField(movie.year).save
    }

    // Parse and persist keywords and relations to movies
    KeywordsParser.parse(keywordsPath, movies, saveMovieKeyword)

    // Parse people
    PersonsCrawler.parse(actorsPath,    movies, Role.Actor,    12338129, savePerson, saveMoviePerson)
    PersonsCrawler.parse(actressesPath, movies, Role.Actor,    7243872,  savePerson, saveMoviePerson)
    PersonsCrawler.parse(directorsPath, movies, Role.Director, 1724653,  savePerson, saveMoviePerson)
    PersonsCrawler.parse(producersPath, movies, Role.Producer, 3719561,  savePerson, saveMoviePerson)

  }

}