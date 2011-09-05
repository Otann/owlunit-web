package com.manymonkeys.service.movie.advanced

import com.manymonkeys.model.cinema._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait AdvancedMovieService {

  def createMovie(movie: Movie): Movie

  def getMostLike(movie: Movie): Map[Movie, Double]

  def createOrUpdateDescription(movie: Movie, description: String): Movie

  def loadByExternalId(service: String, externalId: String): Movie

  def addPerson(movie: Movie, person: Person, role: Role): Movie

  def addKeyword(movie: Movie, keyword: Keyword): Movie

  def addTagline(movie: Movie, tagline: String): Movie

  def addAkaName(movie: Movie, akaName: String, index: Boolean): Movie

  def addTranslateName(movie: Movie, translateName: String, index: Boolean): Movie

  def addGenre(movie: Movie, genre: Genre): Movie

  def addExternalId(movie: Movie, service: String, externalId: String): Movie

}

