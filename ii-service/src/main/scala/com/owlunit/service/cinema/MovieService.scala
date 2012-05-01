package com.owlunit.service.cinema

import com.owlunit.core.ii.IiDao
import impl.MovieServiceImpl

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class MovieIi(override val id: Long,
            val name: String,
            val year: Int,
            val tags: Set[KeywordIi] = Set.empty,
            val persons: Map[Role.Value, Set[PersonIi]] = Map.empty) extends CinemaIi(id, name) {

  def copy(id: Long) = new MovieIi(
    id,
    this.name,
    this.year,
    this.tags,
    this.persons
  )

  override def toString = "MovieIi(%d, %s/%d)" format (id, name, year)

}

object Role extends Enumeration {
  type Role = Value
  val Actor    = Value("Actor")
  val Director = Value("Director")
  val Producer = Value("Producer")
}

trait MovieService {

  def createMovie(sample: MovieIi): MovieIi
  def createMovie(name: String, year: Int): MovieIi
  def loadMovie(id: Long): Option[MovieIi]
  def loadMovie(name: String, year: Int): Option[MovieIi]
  def searchMovie(query: String): Seq[MovieIi]

  def addKeyword(movie: MovieIi, keyword: KeywordIi, startFrequency: Int): MovieIi
  def addPerson(movie: MovieIi, person: PersonIi, newRole: Role.Value): MovieIi

}