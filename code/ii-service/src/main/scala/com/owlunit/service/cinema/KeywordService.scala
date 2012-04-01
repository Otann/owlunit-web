package com.owlunit.service.cinema

import com.owlunit.core.ii.IiDao
import impl.KeywordServiceImpl

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Keyword(override val id: Long, val name: String) extends CinemaItem(id) {

  def copy(id: Long) = new Keyword(id, this.name)

  override def toString = "Keyword(%d, %s)" format (id, name)
}

trait KeywordService {

  def create(name: String): Keyword
  def create(sample: Keyword): Keyword

  def load(name: String): Option[Keyword]
  def loadOrCreate(name: String): Keyword

  def search(prefix: String): Seq[Keyword]

}

object KeywordService {

  def apply(dao: IiDao): KeywordService = new KeywordServiceImpl(dao)

}