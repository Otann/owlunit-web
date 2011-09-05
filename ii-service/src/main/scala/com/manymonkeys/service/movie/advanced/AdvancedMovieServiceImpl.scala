package com.manymonkeys.service.movie.advanced

import com.manymonkeys.model.cinema._
import reflect.BeanProperty
import org.springframework.beans.factory.annotation.Autowired
import com.manymonkeys.core.algo.Recommender
import com.manymonkeys.core.ii.{Ii, IiDao}
import com.manymonkeys.service.movie.advanced.AdvancedMovieServiceImpl._
import scala.collection.JavaConversions._
import scalaj.collection.Imports._


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object AdvancedMovieServiceImpl {

  private val CLASS_KEY = classOf[AdvancedMovieServiceImpl].getName;
  private val CLASS_VALUE = "#"

  private val KEY_NAME = CLASS_KEY + ".NAME"
  private val KEY_NAME_SIMPLIFIED = CLASS_KEY + ".NAME_SIMPLIFIED"
  private val KEY_YEAR = CLASS_KEY + ".YEAR"
  private val KEY_PLOT = CLASS_KEY + ".PLOT"
  private val KEY_EXTERNAL_ID = CLASS_KEY + ".EXTERNAL_ID."

  implicit def iiToMovie(item: Ii): Movie = Movie(item.getMeta(KEY_NAME), item.getMeta(KEY_YEAR).toInt, item.getMeta(KEY_PLOT))

  def simplifyName(original: String) = original.replace("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)", "")
}

class AdvancedMovieServiceImpl extends AdvancedMovieService {

  @BeanProperty
  @Autowired
  var dao: IiDao = null //TODO Anton Chebotaev - move to ServiceDaoImpl trait

  @BeanProperty
  @Autowired
  var recommender: Recommender = null //TODO Anton Chebotaev - move to ServiceRecommenderImpl trait

  @BeanProperty
  var initialPersonWeight = 10.0

  @BeanProperty
  var initialKeywordWeight = 10.0

  implicit def movieToIi(movie: Movie): Ii = { //TODO Anton Chebotaev - move to MovieServiceConversion trait
    val items = dao.load(KEY_NAME_SIMPLIFIED, simplifyName(movie.name))
    if (items.isEmpty) {
      null
    } else {
      items.iterator().next()
    }
  }

  implicit def personToIi(person: Person): Ii = null //TODO Anton Chebotaev - move to PersonServiceConvertions trait

  implicit def keywordToIi(keyWord: Keyword): Ii = null //TODO Anton Chebotaev - move to KeywordServiceConvertions trait

  def createMovie(movie: Movie): Movie = {
    val item = dao.createInformationItem()
    dao.setUnindexedMeta(item, CLASS_KEY, CLASS_VALUE)
    dao.setUnindexedMeta(item, KEY_NAME_SIMPLIFIED, simplifyName(movie.name))
    dao.setMeta(item, KEY_NAME, movie.name)
    dao.setUnindexedMeta(item, KEY_YEAR, movie.year.toString)
    dao.setUnindexedMeta(item, KEY_PLOT, movie.description)
  }

  def getMostLike(movie: Movie) = recommender.getMostLike(movie, dao).asScala.map({case (k,v) => (k.asInstanceOf[Movie], v)}).toMap // false negative

  def createOrUpdateDescription(movie: Movie, description: String) = dao.setMeta(movie, KEY_PLOT, description)

  def loadByExternalId(service: String, externalId: String) = {
    val items = dao.load(KEY_EXTERNAL_ID + service, externalId)
    if (items.isEmpty) {
      null
    } else {
      items.iterator().next()
    }
  }

  def addPerson(movie: Movie, person: Person, role: Role) = dao.setComponentWeight(movie, person, initialPersonWeight)

  def addKeyword(movie: Movie, keyword: Keyword) = dao.setComponentWeight(movie, keyword, initialKeywordWeight)

  def addTagline(movie: Movie, tagline: String) = null

  def addAkaName(movie: Movie, akaName: String, index: Boolean) = null

  def addTranslateName(movie: Movie, translateName: String, index: Boolean) = null

  def addGenre(movie: Movie, genre: Genre) = null

  def addExternalId(movie: Movie, service: String, externalId: String) = null
}