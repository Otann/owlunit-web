package com.owlunit.web.lib.stub

import com.owlunit.service.cinema._


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MovieServiceStub extends MovieServiceStub {

  val tron = new MovieIi(0, "Tron: Legacy", 2011,
    Set(KeywordServiceStub.k1, KeywordServiceStub.k2),
    Map(
      Role.Actor    -> Set(PersonServiceStub.ford, PersonServiceStub.pacino),
      Role.Director -> Set(PersonServiceStub.lukas),
      Role.Producer -> Set(PersonServiceStub.lukas)
    )
  )

}

trait MovieServiceStub extends MovieService {
  import MovieServiceStub._

  def createMovie(sample: MovieIi) = tron

  def createMovie(name: String, year: Int) = tron

  def loadMovie(id: Long) = Some(tron)

  def loadMovie(name: String, year: Int) = Some(tron)

  def searchMovie(query: String) = List(tron)

  def addKeyword(movie: MovieIi, keyword: KeywordIi, startFrequency: Int) = tron

  def addPerson(movie: MovieIi, person: PersonIi, newRole: Role.Value) = tron

}