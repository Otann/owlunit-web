package com.owlunit.mining.tmdb

import net.liftweb.common._
import com.owlunit.web.lib.JValueHelpers._
import com.owlunit.web.model._
import net.liftweb.common.Full
import net.liftweb.json.JsonAST.JValue
import java.util.Date
import net.liftweb.mongodb.record.MongoMetaRecord
import common.{TmdbMetaRecord, IiTagRecord, IiTagMetaRecord}
import net.liftweb.json._
import net.liftweb.common.Full
import net.liftweb.util.Helpers._
import net.liftweb.common.Full
import net.liftweb.common.Full

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
object Extractor extends Loggable {

  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")

  def extractMovie(raw: JValue): Box[Movie] = extractRecord(Movie)(raw){ movie =>

  // Update all optional fields here, call child updates
    update[String](raw) (_ \ "title")         (movie.title(_))
    update[String](raw) (_ \ "poster_path")   (movie.posterUrl(_))
    update[String](raw) (_ \ "backdrop_path") (movie.backdropUrl(_))
    update[String](raw) (_ \ "tagline")       (movie.tagline(_))
    update[String](raw) (_ \ "overview")      (movie.overview(_))
    update[String](raw) (_ \ "release_date")  (x => movie.release(dateFormat.parse(x)))

    for {
      rawKeyword <- (raw \ "keywords" \ "keywords").children
      keyword    <- extractKeyword(rawKeyword)
    } {
      movie.addKeyword(keyword)
    }

    for {
      crewRaw    <- (raw \ "casts" \ "crew").children
      crewRecord <- extractCrew(crewRaw)
    } {
      movie.crew(crewRecord :: movie.crew.is)
    }

    for {
      castRaw    <- (raw \ "casts" \ "cast").children
      castRecord <- extractCast(castRaw)
    } {
      movie.cast(castRecord :: movie.cast.is)
    }

    movie.save(safe = true)
  }


  def extractKeyword(raw: JValue) = extractRecord(Keyword)(raw){ keyword =>
    update[String](raw) (_ \ "name") (keyword.nameField(_))
    keyword.save(safe = true)
  }

  def extractPerson(raw: JValue) = extractRecord(Person)(raw){ person =>

    update[String](raw) (_ \ "name")          (person.fullName(_))
    update[String](raw) (_ \ "profile_path")  (person.photoUrl(_))
    update[String](raw) (_ \ "biography")     (person.bio(_))

    update[String](raw) (_ \ "birthday")  (x => person.birthday(dateFormat.parse(x)))
    update[String](raw) (_ \ "deathday")  (x => person.deathday(dateFormat.parse(x)))

    person.save(safe = true)

  }

  def extractCast(raw: JValue): Box[CastItem] = for {
    personRecord  <- extractPerson(raw)
    order         <- extract[BigInt](raw)(_ \ "order").map(_.longValue())
    castId        <- extract[BigInt](raw)(_ \ "cast_id").map(_.longValue())
    character     <- extract[String](raw)(_ \ "character")
  } yield {
    val result = CastItem.createRecord
    result.person(personRecord.id.is)
    result.character(character)
    result.castId(castId)
    result.order(order)
    result
  }

  def extractCrew(raw: JValue): Box[CrewItem] = {
    for {
      person        <- extractPerson(raw)
      job           <- extract[String](raw)(_ \ "job")
      departament   <- extract[String](raw)(_ \ "department")
    } yield {
      val result = CrewItem.createRecord
      result.person(person.id.is)
      result.job(job)
      result.department(departament)
      result
    }
  }

  private def extractRecord[T <: IiTagRecord[T]](meta: TmdbMetaRecord[T])(raw: JValue)(updater: T => T): Box[T] = for {
    tmdbId <- extract[BigInt](raw)(_ \ "id").map(_.longValue()) //?
  } yield {
    val result: T = meta.findByTMDB(tmdbId) match {
      case Full(record) => record
      case _            => meta.createTmdbRecord(tmdbId)
    }
    updater(result)
  }

}
