package com.owlunit.service.cinema.impl

import com.owlunit.service.cinema.{MovieIi, CinemaIi, CinemaService}
import com.owlunit.core.ii.{NotFoundException, DAOException, Ii, IiDao}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class CinemaServiceImpl(val dao: IiDao) extends CinemaService with MovieServiceImpl with KeywordServiceImpl with PersonServiceImpl {
  import CinemaServiceImpl._
  
  def search(query: String) = {
    val results = for (item <- dao.search(KeySearch, buildQuery(query))) yield extractOne(dao, item)
    results.flatten
  }
  
  def load(id: Long) = {
    try {
      extractOne(dao, dao.load(id))
    } catch {
      case ex: NotFoundException => None
    }
  }

  //TODO Anton Chebotaev - fix building of query
  def similarMovies(query: Map[CinemaIi, Double]): Map[MovieIi, Double] = {
    val internalQuery = collection.mutable.Map[Ii, Double]()
    for ((k, v) <- query) k match {
      case x: MovieIi => internalQuery ++= dao.loadComponents(dao.load(x.id)).components.get
      case x => internalQuery += dao.load(x.id) -> v
    }
    val result = dao.recommender.getSimilar(internalQuery.toMap, MovieServiceImpl.MetaKeyPrefix)
    result.map{ case (k, v) => MovieServiceImpl.extractOne(dao, k).get -> v}
  }
  
}

object CinemaServiceImpl {

  private[cinema] val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeySearch     = MetaKeyPrefix + ".Search"

  def extractOne(dao: IiDao, item: Ii): Option[CinemaIi] = {
    MovieServiceImpl.extractOne(dao, item) orElse 
    PersonServiceImpl.extractOne(dao, item) orElse
    KeywordServiceImpl.extractOne(dao, item)
  }
  
}