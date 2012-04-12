package com.owlunit.web.lib.stub

import com.owlunit.service.cinema.{KeywordIi, KeywordService}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object KeywordServiceStub extends KeywordServiceStub {

  val k1 = new KeywordIi(1, "KeywordIi One")
  val k2 = new KeywordIi(2, "KeywordIi Two")

}

trait KeywordServiceStub extends KeywordService {
  import KeywordServiceStub._

  def createKeyword(name: String) = k1

  def createKeyword(sample: KeywordIi) = k1
  
  def loadKeyword(id: Long) = Some(k1)

  def loadKeyword(name: String) = Some(k1)

  def loadOrCreateKeyword(name: String) = k1

  def searchKeyword(prefix: String) = List(k1, k2)

}