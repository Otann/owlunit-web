package com.manymonkeys.research.service.cinema.impl.MovieService

import com.manymonkeys.model.cinema._
import reflect.BeanProperty
import com.manymonkeys.core.ii.Ii
import com.manymonkeys.research.service.cinema.MovieService
import com.manymonkeys.research.service.utils.{RecommenderAccess, IiDaoAccess}
import com.manymonkeys.research.service.cinema.impl.PersonService.PersonConverter
import com.manymonkeys.research.service.cinema.impl.KeywordService.KeywordConverter
import scalaj.collection.Imports._
import MovieServiceImpl._


/**
* @author Anton Chebotaev
*         Owls Proprietary
*/

object MovieServiceImpl {

  final val CREATOR_KEY         = classOf[MovieService].getName
  final val CREATOR_VALUE       = "#"

  final val KEY_NAME            = classOf[MovieService].getName + ".NAME"
  final val KEY_NAME_SIMPLIFIED = classOf[MovieService].getName + ".NAME_SIMPLIFIED"
  final val KEY_YEAR            = classOf[MovieService].getName + ".YEAR"
  final val KEY_PLOT            = classOf[MovieService].getName + ".PLOT"
  final val KEY_AKA_NAME        = classOf[MovieService].getName + ".AKA_NAME"
  final val KEY_TRANSLATE_NAME  = classOf[MovieService].getName + ".TRANSLATE_NAME"

  final val KEY_EXTERNAL_ID     = classOf[MovieService].getName + ".EXTERNAL_ID."

}

class MovieServiceImpl extends MovieService
                       with IiDaoAccess
                       with RecommenderAccess
                       with MovieConverter
                       with PersonConverter
                       with KeywordConverter {

  @BeanProperty
  var initialPersonWeight = 10.0

  @BeanProperty
  var initialKeywordWeight = 10.0

  implicit def iiToMovie(item: Ii): Movie = {
    val meta = itemWithMeta(item)
    Movie(
      uuid = item.getUUID,
      name = meta.getMeta(KEY_NAME),
      year = meta.getMeta(KEY_YEAR).toInt,
      description = meta.getMeta(KEY_PLOT)
    )
  }

  def createMovie(movie: Movie): Movie = {
    val item = dao.createInformationItem()
    dao.setMeta(item, KEY_NAME, movie.name)
    dao.setUnindexedMeta(item, KEY_NAME_SIMPLIFIED, simpleName(movie.name, movie.year))
    dao.setUnindexedMeta(item, KEY_YEAR, movie.year.toString)
    dao.setUnindexedMeta(item, KEY_PLOT, movie.description)
  }

  def loadByName(name: String, year: Long) = {
    val items = dao.load(KEY_NAME_SIMPLIFIED, simpleName(name, year))
    if (items.isEmpty) {
      throw new NotFoundException("%s (%d)" format (name, year))
    } else {
      items.iterator().next()
    }
  }

  def loadByExternalId(service: String, externalId: String) = {
    val items = dao.load(KEY_EXTERNAL_ID + service, externalId)
    if (items.isEmpty) {
      throw new NotFoundException("%s@%s" format  (externalId, service))
    } else {
      items.iterator().next()
    }
  }

  def getMostLike(movie: Movie) =  recommend(movie, dao, isMovie).map({case (item, value) => (iiToMovie(item), value)})

  def createOrUpdateDescription(movie: Movie, description: String) = dao.setMeta(movie, KEY_PLOT, description)

  def addPerson(movie: Movie, person: Person, role: Role) = dao.setComponentWeight(movie, person, initialPersonWeight)

  def addKeyword(movie: Movie, keyword: Keyword) = dao.setComponentWeight(movie, keyword, initialKeywordWeight)

  def hasKeyword(movie: Movie, keyword: Keyword) = {
    val components = dao.loadComponents(movie).getComponents.asScala
    val keywordItem = keywordToIi(keyword)
    components.contains(keywordItem)
  }

  def addTagline(movie: Movie, tagline: String) = movie //TODO Anton Chebotaev - implement

  def setAkaName(movie: Movie, akaName: String) = dao.setMeta(movie, KEY_AKA_NAME, akaName)

  def setTranslateName(movie: Movie, translateName: String) = dao.setMeta(movie, KEY_TRANSLATE_NAME, translateName)

  def addExternalId(movie: Movie, service: String, externalId: String) = dao.setUnindexedMeta(movie, KEY_EXTERNAL_ID + service, externalId)

  /*
          Private
   */

  private def isMovie(item: Ii) = itemWithMeta(item).getMeta(CREATOR_KEY) != null

}