package com.owlunit.web.lib.stub

import com.owlunit.service.cinema.{CinemaIi, CinemaService}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object CinemaServiceStub extends CinemaServiceStub { }

trait CinemaServiceStub extends CinemaService with KeywordServiceStub with MovieServiceStub with PersonServiceStub {

  def search(query: String) = List(
    MovieServiceStub.tron,
    KeywordServiceStub.k1,
    PersonServiceStub.pacino,
    KeywordServiceStub.k2,
    PersonServiceStub.ford,
    PersonServiceStub.lukas
  )


  def load(id: Long) = Some(MovieServiceStub.tron)

  def similarMovies(query: Map[CinemaIi, Double]) = Map(
    MovieServiceStub.tron -> 100
  )

}