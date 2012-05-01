package com.owlunit.web.lib.stub

import com.owlunit.service.cinema.{CinemaIi, RecommenderService}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait RecommenderStub extends RecommenderService {

  def similarMovies(query: Map[CinemaIi, Double]) = Map(MovieServiceStub.tron -> 90)

}