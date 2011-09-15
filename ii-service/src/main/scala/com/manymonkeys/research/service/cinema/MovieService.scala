package com.manymonkeys.research.service.cinema

import com.manymonkeys.model.cinema._

/**
* @author Ilya Pimenov
*         Owls Proprietary
*/
trait MovieService {

  def createMovie(movie: Movie): Movie

  def loadByName(name: String, year: Long): Movie

  def getMostLike(movie: Movie): Map[Movie, Double]

  def createOrUpdateDescription(movie: Movie, description: String): Movie

  def loadByExternalId(service: String, externalId: String): Movie

  def addPerson(movie: Movie, person: Person, role: Role): Movie

  def addKeyword(movie: Movie, keyword: Keyword): Movie

  def hasKeyword(movie: Movie, keyword: Keyword): Boolean

  def addTagline(movie: Movie, tagline: String): Movie

  def setAkaName(movie: Movie, akaName: String): Movie

  def setTranslateName(movie: Movie, translateName: String): Movie

  def addExternalId(movie: Movie, service: String, externalId: String): Movie

}

