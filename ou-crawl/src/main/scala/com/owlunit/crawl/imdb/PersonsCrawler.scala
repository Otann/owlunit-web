package com.owlunit.crawl.imdb

import io.Source
import com.owlunit.crawl.Counter
import com.owlunit.service.cinema.{PersonIi, MovieIi, CinemaService, Role}
import collection.mutable.{ListBuffer, Map => MutableMap}
import com.weiglewilczek.slf4s.Logging

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class PersonsCrawler(sourcePath: String, cinemaService: CinemaService, role: Role.Value, totalLines: Int) extends  Logging {

  val personMovieExtractor = """^([^\t]+)\t+(.+)\((\d+)\).*$""".r
  val movieExtractor       = """^\t\t\t(.+)\((\d+)\).*$""".r

  def run() {

    val personTimer = Counter.start()
    val linesTimer = Counter.start(totalLines)
    val source = Source.fromFile(sourcePath).getLines()

    var prePerson: String = null
    var preMovies = ListBuffer[MovieIi]()

    while (source.hasNext) {
      val line = source.next()
      linesTimer.tick(logger, 100000, "lines so far")
      try {

        if (personMovieExtractor.findAllIn(line).length > 0) {
          // new person, flush previous
          if (prePerson != null && !preMovies.isEmpty) {

            val name = prePerson.split(", ")
            val person = cinemaService.createPerson(new PersonIi(0, name(1), name(0)))
            personTimer.tick(logger, 10000, "persons of role " + role.toString)
            for (movie <- preMovies) {
              cinemaService.addPerson(movie, person, role)
            }
          }
          prePerson = null;
          preMovies = ListBuffer[MovieIi]()

          val personMovieExtractor(personName, movieName, yearRaw) = line
          val year = yearRaw.toInt
          prePerson = personName

          val movie = cinemaService.loadMovie(movieName, year)
          movie.foreach(preMovies += _)

        } else if (movieExtractor.findAllIn(line).length > 0 && prePerson != null) {
          val movieExtractor(movieName, yearRaw) = line
          val year = yearRaw.toInt

          val movie = cinemaService.loadMovie(movieName, year)
          movie.foreach(preMovies += _)
        }

      } catch {
        case ex: Exception => logger.error("Catched unhandeled exception %s for line %s" format (ex, line))
      }
    }

  }

}