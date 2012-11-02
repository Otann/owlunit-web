package com.owlunit.crawl.parser.imdb

import io.Source
import com.owlunit.crawl._
import com.weiglewilczek.slf4s.Logging
import lib.Counter
import model.{PlainPerson, PlainMovie}
import com.owlunit.web.model.{Person, Role}
import parser.ParserHelper

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object PersonsCrawler extends ParserHelper with Logging {

  val personMovieExtractor = """^([^\t]+)\t+([^\()]+)\((\d+)\).*$""".r
  val movieExtractor       = """^\t\t\t(.+)\((\d+)\).*$""".r

  def parse( path: String,
             movieSimpleNames: collection.mutable.Map[String, PlainMovie],
             role: Role.Role,
             totalLines: Int,
             flushPerson: (PlainPerson) => Person,
             flushRelation: (PlainMovie, Person, Role.Role) => Any
             ) {

    val personTimer = Counter.start()
    val linesTimer = Counter.start(totalLines)
    val source = Source.fromFile(path, "latin1").getLines()

    var prePerson: String = null
    var preMovies = collection.mutable.ListBuffer[PlainMovie]()

    def flushLocal(fullName: String) {
      if (prePerson != null && !preMovies.isEmpty) {

        val name = prePerson.split(", ")
        val psPerson = if (name.length > 1) PlainPerson(name(1), name(0)) else PlainPerson(fullName, "")
        personTimer.tick(logger, 10000, "persons of role " + role)

        val person = flushPerson(psPerson)
        for (movie <- preMovies) {
          flushRelation(movie, person, role)
        }
      }
    }

    def loadMovie(name: String, year: Int) = {
      val s = simplifyName(name, year)
      if (movieSimpleNames.contains(s)) Some(PlainMovie(name, year)) else None
    }

    while (source.hasNext) {
      val line = source.next()
      linesTimer.tick(logger, 1000000, "lines processed so far")
      try {

        if (personMovieExtractor.findAllIn(line).length > 0) {

          // new person, flush previous
          flushLocal(prePerson)
          prePerson = null
          preMovies = collection.mutable.ListBuffer[PlainMovie]()

          val personMovieExtractor(personName, movieName, yearRaw) = line
          val year = yearRaw.toInt
          prePerson = personName

          val movie = loadMovie(movieName, year)
          movie.foreach(preMovies += _)

        } else if (movieExtractor.findAllIn(line).length > 0 && prePerson != null) {
          val movieExtractor(movieNameRaw, yearRaw) = line
          val movieName = movieNameRaw.trim
          val year = yearRaw.toInt

          val movie = loadMovie(movieName, year)
          movie.foreach(preMovies += _)
        }

      } catch {
        case ex: Exception => println("ERROR: " + "Unhandeled exception %s for line %s" format (ex, line))
      }
    }
    flushLocal(prePerson)

  }

}