package com.owlunit.service.cinema.impl

import com.owlunit.core.ii.{Ii, IiDao}
import collection.mutable.ListBuffer
import com.owlunit.service.cinema._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MovieServiceImpl {


  private val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeyName         = MetaKeyPrefix + ".NAME"
  private[cinema] val KeyYear         = MetaKeyPrefix + ".YEAR"
  private[cinema] val KeySimpleName   = MetaKeyPrefix + ".SIMPLE_NAME"
  private[cinema] val KeyDescription  = MetaKeyPrefix + ".DESCRIPTION"
  private[cinema] val KeyPersonIds    = MetaKeyPrefix + ".PERSON"

  private[cinema] val PersonSeparator = "#"
  private[cinema] val RoleSeparator = "@"

  def apply(dao: IiDao) = new MovieServiceImpl(dao)

  private[cinema] def extractOne(dao: IiDao, item: Ii): Option[Movie] = {
    val ii = withComponents(dao, withMeta(dao, item))
    ii.metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some {

        val components = for (component <- ii.components.get.keys.toSeq) yield withMeta(dao, component)
        val map = unpackPersons(ii.metaValue(KeyPersonIds).getOrElse(""))
        val allPersons = PersonServiceImpl.extract(dao, components)

        val personsRaw = for (role <- Role.values) yield
          role -> allPersons.filter(p => map.getOrElse(p.id, Set()).contains(role)).toSet

        new Movie(
          id          = ii.id,
          name        = ii.metaValue(KeyName).get,
          year        = ii.metaValue(KeyYear).get.toInt,
          description = ii.metaValue(KeyDescription).get,
          tags        = KeywordServiceImpl.extract(dao, components).toSet,
          persons     = personsRaw.toMap
        )

      }
    }
  }

  private[cinema] def packPersons (map: Map[Long, Set[Role.Value]]): String = {
    val persons = for (pair <- map) yield {
      "%d%s%s" format (pair._1, RoleSeparator, pair._2.mkString(RoleSeparator))
    }
    persons.mkString(PersonSeparator)
  }

  private[cinema] def unpackPersons (string: String): Map[Long, Set[Role.Value]] = {
    if (string == "") return Map()
    val persons: Array[String] = string.split(PersonSeparator)
    val flatMap = for (person <- persons) yield {
      val p = person.split(RoleSeparator).head.toLong
      val r = person.split(RoleSeparator).tail.map(role => Role.withName(role)).toSet
      p -> r
    }
    flatMap.toMap
  }

  private def simplifyName(title: String, year: Int) = simplifyComplexName(title, "##", year)
}

class MovieServiceImpl(dao: IiDao) extends MovieService {
  import MovieServiceImpl._

  //TODO Anton Chebotaev - move to some other place
  val maxCounter = 1000;
  val maxWeight = 50.0;
  val minWeight = 10.0;
  val roleWeight = Map[Role.Value, Double] (
    Role.Actor    -> 50.0,
    Role.Director -> 60.0,
    Role.Producer -> 60.0
  )

  def create(sample: Movie): Movie = {
    val item = dao.createIi
    dao.setMetaUnindexed(item, MetaKeyPrefix, "#")

    dao.setMeta(item, KeyName, sample.name)
    dao.setMeta(item, KeySimpleName, simplifyName(sample.name, sample.year))
    dao.setMetaUnindexed(item, KeyYear, sample.year.toString)
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

  def search(query: String) = {
    val items = dao.search(KeyName, "%s*" format query)
    val iterator = items.iterator
    val result = ListBuffer[Movie]()
    while (iterator.hasNext) {
      val k = extractOne(dao, iterator.next())
      if (k.isDefined)
        result += k.get
    }

    result
  }

  def addKeyword(movie: Movie, keyword: Keyword, startFrequency: Int): Movie = {
    val movieIi = dao.load(movie.id)
    val keywordIi = dao.load(keyword.id)

    val amount = startFrequency.toDouble min maxCounter
    val value = maxWeight - (amount / maxCounter) * (maxWeight - minWeight)

    val updated = dao.setComponentWeight(movieIi, keywordIi, value)
    extractOne(dao, updated).get // TODO possible update here
  }

  def addPerson(movie: Movie, person: Person, newRole: Role.Value): Movie = {

    if (movie.persons(newRole).contains(person))
      return movie

    var weight = 0.0
    for (role <- movie.persons.keys)
      if (movie.persons(role).contains(person))
        weight += roleWeight(role)
    weight += roleWeight(newRole)

    val updated = dao.setComponentWeight(dao.load(movie.id), dao.load(person.id), weight)
    extractOne(dao, updated).get // TODO possible update here

  }

}





