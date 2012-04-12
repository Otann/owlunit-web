package com.owlunit.service.cinema

import com.owlunit.core.ii.IiDao
import impl.KeywordServiceImpl

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class KeywordIi(override val id: Long, val name: String) extends CinemaIi(id, name) {
  def copy(id: Long) = new KeywordIi(id, this.name)
  override def toString = "KeywordIi(%d, %s)" format (id, name)
}

trait KeywordService {

  def createKeyword(name: String): KeywordIi
  def createKeyword(sample: KeywordIi): KeywordIi

  def loadKeyword(id: Long): Option[KeywordIi]
  def loadKeyword(name: String): Option[KeywordIi]
  def loadOrCreateKeyword(name: String): KeywordIi

  def searchKeyword(prefix: String): Seq[KeywordIi]

}