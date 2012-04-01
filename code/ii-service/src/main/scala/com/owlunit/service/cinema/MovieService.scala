package com.owlunit.service.cinema

import com.owlunit.core.ii.IiDao
import impl.MovieServiceImpl

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Movie(override val id: Long,
            val name: String,
            val year: Int,
            val description: String = "",
            val tags: Set[Keyword] = Set.empty,
            val persons: Map[Role.Value, Set[Person]] = Map.empty) extends CinemaItem(id) {

  def copy(id: Long) = new Movie(
    id,
    this.name,
    this.year,
    this.description,
    this.tags,
    this.persons
  )

  override def toString = "Movie(%d, %s/%d)" format (id, name, year)

}

object Role extends Enumeration {
  type Role = Value
  val Actor    = Value("Actor")
  val Director = Value("Director")
  val Producer = Value("Producer")
}

trait MovieService {

  def create(sample: Movie): Movie
  def create(name: String, year: Int): Movie
  def load(name: String, year: Int): Option[Movie]
  def search(query: String): Seq[Movie]
  def addKeyword(movie: Movie, keyword: Keyword, startFrequency: Int): Movie
  def addPerson(movie: Movie, person: Person, newRole: Role.Value): Movie

}

object MovieService {

  def apply(dao: IiDao): MovieService = new MovieServiceImpl(dao)

}