package com.owlunit.service.cinema

import com.owlunit.core.ii.{Ii, IiDao}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie(override val id: Long,
            val name: String,
            val year: Int,
            val description: String = "",
            val tags:      Set[Keyword] = Set.empty,
            val actors:    Set[Person]  = Set.empty,
            val directors: Set[Person]  = Set.empty,
            val producers: Set[Person]  = Set.empty) extends CinemaItem(id) {

  def copy(id: Long) = new Movie(
    id,
    this.name,
    this.year,
    this.description,
    this.tags,
    this.actors,
    this.directors,
    this.producers
  )

}

object Role extends Enumeration {
  type Role = Value
  val Actor    = Value("Actor")
  val Director = Value("Director")
  val Producer = Value("Producer")
}

object MovieService {

  private val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeyName         = MetaKeyPrefix + ".NAME"
  private[cinema] val KeyYear         = MetaKeyPrefix + ".YEAR"
  private[cinema] val KeySimpleName   = MetaKeyPrefix + ".SIMPLE_NAME"
  private[cinema] val KeyDescription  = MetaKeyPrefix + ".DESCRIPTION"
  private[cinema] val KeyPersonIds    = MetaKeyPrefix + ".PERSON"

  private[cinema] val PersonSeparator = "#"
  private[cinema] val RoleSeparator = "@"


  def extractOne(dao: IiDao, item: Ii): Option[Movie] = {
    val ii = withComponents(dao, withMeta(dao, item))
    ii.metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some {

        val components = for (c <- ii.components.get.keys.toSeq) yield withMeta(dao, c)
        val map = unpackPersons(ii.metaValue(KeyPersonIds).get)
        val persons = PersonService.extract(dao, components)

        new Movie(
          id          = item.id,
          name        = item.metaValue(KeyName).get,
          year        = item.metaValue(KeyYear).get.toInt,
          description = item.metaValue(KeyDescription).get,
          tags        = KeywordService.extract(dao, components).toSet,
          actors      = persons.filter(p => {map.getOrElse(p.id, null) == Role.Actor}).toSet,
          directors   = persons.filter(p => {map.getOrElse(p.id, null) == Role.Director}).toSet,
          producers   = persons.filter(p => {map.getOrElse(p.id, null) == Role.Producer}).toSet
        )

      }
    }
  }

  private[cinema] def packPersons (map: Map[Long,  Role.Value]): String =
    map.map{case(k, v) => "%d%s%s" format (k, RoleSeparator, v)}.mkString(PersonSeparator)

  private[cinema] def unpackPersons (string: String): Map[Long,  Role.Value] = {
    val pairs: Array[Array[String]] = string.split(PersonSeparator).map(_.split(RoleSeparator))
    val flatMap = for (pair <- pairs) yield pair match {
      case Array(first, second) => first.toString.toLong -> Role.withName(second.toString)
    }
    flatMap.toMap
  }

  private def simplifyName(title: String, year: Int) = simplifyComplexName(title, "##", year)
}

class MovieService(dao: IiDao) {
  import MovieService._

  def create(sample: Movie):Movie = {
    val item = dao.createIi
    dao.setMetaUnindexed(item, MetaKeyPrefix, "#")

    dao.setMeta(item, KeyName, sample.name)
    dao.setMetaUnindexed(item, KeyYear, sample.year.toString)
    dao.setMetaUnindexed(item, KeySimpleName, simplifyName(sample.name, sample.year))
    dao.setMetaUnindexed(item, KeyDescription, sample.description)

    sample.copy(id = item.id)
  }

  def load(name: String, year: Int): Option[Movie] = {
    val items = dao.load(KeySimpleName, simplifyName(name, year))
    items.size match {
      case 0 => None
      case 1 => extractOne(dao, items.iterator.next())
      case _ => throw new CinemaException("Ambiguous load")
    }
  }

}
