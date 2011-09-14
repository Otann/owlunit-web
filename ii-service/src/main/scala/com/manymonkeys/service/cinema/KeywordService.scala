package com.manymonkeys.service.cinema

import com.manymonkeys.model.cinema.Keyword
import java.util.UUID

/**
* @author Ilya Pimenov
*         Owls Proprietary
*/
trait KeywordService {

  def createKeyword(name: String): Keyword

  def loadKeyword(uuid: UUID): Keyword

  def loadKeyword(name: String): Keyword

  def listKeywords: Seq[Keyword]

  def updateName(keyword: Keyword, name: String): Keyword

  def isKeyword(keyword: Keyword): Boolean

}

