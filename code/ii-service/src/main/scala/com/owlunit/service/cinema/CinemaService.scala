package com.owlunit.service.cinema

import com.owlunit.core.ii.IiDao
import impl.CinemaServiceImpl

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class CinemaIi (val id: Long, val detail: String) {

  override def hashCode() = id.hashCode()

  override def equals(p: Any) = p.isInstanceOf[CinemaIi] && p.asInstanceOf[CinemaIi].id == id

  override def toString = "%s(%d, %s)" format (this.getClass.getSimpleName, id, detail)

}

object CinemaIi {
  
  def apply(id: Long,  detail: String) = new CinemaIi(id, detail)
  def unapply(item: CinemaIi) = Some(item.id, item.detail)
  
}

trait CinemaService extends MovieService with KeywordService with PersonService with RecommenderService {

  def search(query: String): Seq[CinemaIi]
  def load(id: Long): Option[CinemaIi]

}

object CinemaService {

  def apply(dao: IiDao): CinemaService = new CinemaServiceImpl(dao)

}