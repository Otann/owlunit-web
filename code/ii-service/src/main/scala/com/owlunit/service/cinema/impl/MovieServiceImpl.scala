package com.owlunit.service.cinema.impl

import com.owlunit.core.ii.{Ii, IiDao}
import collection.mutable.ListBuffer
import com.owlunit.service.cinema._
import exception.CinemaException

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MovieServiceImpl {


  private[cinema] val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeyName         = MetaKeyPrefix + ".NAME"
  private[cinema] val KeyYear         = MetaKeyPrefix + ".YEAR"
  private[cinema] val KeySearch       = MetaKeyPrefix + ".SEARCH"
  private[cinema] val KeyIndex        = MetaKeyPrefix + ".SIMPLE_NAME"
  private[cinema] val KeyDescription  = MetaKeyPrefix + ".DESCRIPTION"
  private[cinema] val KeyPersonIds    = MetaKeyPrefix + ".PERSON"

  private[cinema] val PersonSeparator = "#"
  private[cinema] val RoleSeparator = "@"

  private[cinema] def extractOne(dao: IiDao, item: Ii): Option[MovieIi] = {
    val ii = withComponents(dao, withMeta(dao, item))
    ii.metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some {

        val components = for (component <- ii.components.get.keys.toSeq) yield withMeta(dao, component)
        val map = unpackPersons(ii.metaValue(KeyPersonIds).getOrElse(""))
        val allPersons = PersonServiceImpl.extract(dao, components)

        val personsRaw = for (role <- Role.values) yield
          role -> allPersons.filter(p => map.getOrElse(p.id, Set()).contains(role)).toSet

        new MovieIi(
          id          = ii.id,
          name        = ii.metaValue(KeyName).get,
          year        = ii.metaValue(KeyYear).get.toInt,
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

trait MovieServiceImpl extends MovieService {
  import MovieServiceImpl._

  def dao: IiDao

  //TODO Anton Chebotaev - move to some other place
  val maxCounter = 1000;
  val maxWeight = 50.0;
  val minWeight = 10.0;
  val roleWeight = Map[Role.Value, Double] (
    Role.Actor    -> 50.0,
    Role.Director -> 60.0,
    Role.Producer -> 60.0
  )

  def createMovie(name: String, year: Int): MovieIi = createMovie(new MovieIi(0, name, year))

  def createMovie(sample: MovieIi): MovieIi = {
    val item = dao.createIi
    dao.setMetaUnindexed(item, MetaKeyPrefix, "#")

    dao.setMeta(item, KeySearch, sample.name.toLowerCase)
    dao.setMeta(item, CinemaServiceImpl.KeySearch, sample.name.toLowerCase)
    dao.setMeta(item, KeyIndex, simplifyName(sample.name, sample.year))
    dao.setMetaUnindexed(item, KeyName, sample.name)
    dao.setMetaUnindexed(item, KeyYear, sample.year.toString)

    sample.copy(id = item.id)
  }

  def loadMovie(id: Long): Option[MovieIi] = extractOne(dao, dao.load(id))

  def loadMovie(name: String, year: Int): Option[MovieIi] = {
    val items = dao.load(KeyIndex, simplifyName(name, year))
    items.size match {
      case 0 => None
      case 1 => extractOne(dao, items.iterator.next())
      case _ => throw new CinemaException("Ambiguous load")
    }
  }

  def searchMovie(query: String) = {
    val items = dao.search(KeySearch, buildQuery(query))
    val iterator = items.iterator
    val result = ListBuffer[MovieIi]()
    while (iterator.hasNext) {
      val k = extractOne(dao, iterator.next())
      if (k.isDefined)
        result += k.get
    }

    result
  }

  def addKeyword(movie: MovieIi, keyword: KeywordIi, startFrequency: Int): MovieIi = {
    val movieIi = dao.load(movie.id)
    val keywordIi = dao.load(keyword.id)

    val amount = startFrequency.toDouble min maxCounter
    val value = maxWeight - (amount / maxCounter) * (maxWeight - minWeight)

    val updated = dao.setComponentWeight(movieIi, keywordIi, value)
    extractOne(dao, updated).get // TODO possible update here
  }

  def addPerson(movie: MovieIi, person: PersonIi, newRole: Role.Value): MovieIi = {

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





