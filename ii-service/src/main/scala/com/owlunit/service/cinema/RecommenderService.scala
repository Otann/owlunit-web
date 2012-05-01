package com.owlunit.service.cinema

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait RecommenderService {

  def similarMovies(query: Map[CinemaIi, Double]): Map[MovieIi, Double]
  
}