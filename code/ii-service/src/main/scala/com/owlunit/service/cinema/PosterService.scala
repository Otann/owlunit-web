package com.owlunit.service.cinema

import java.net.URL

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object PosterService {

  def getPosterUrl(movie: Movie):URL = new URL("http://placehold.it/200x300");

}