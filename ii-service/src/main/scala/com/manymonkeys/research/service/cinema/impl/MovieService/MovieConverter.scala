package com.manymonkeys.research.service.cinema.impl.MovieService

import com.manymonkeys.model.cinema.Movie
import com.manymonkeys.core.ii.Ii
import com.manymonkeys.research.service.utils.IiDaoAccess
import MovieServiceImpl._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
trait MovieConverter extends IiDaoAccess {

  class MovieNotFoundException(movie: Movie) extends Exception("Referring movie %s (%d) was not found in service" format (movie.name, movie.year)) { }

  implicit def movieToIi(movie: Movie): Ii = {
    if (movie.uuid != null) {
      val item = dao.load(movie.uuid)
      if (item == null) {
        throw new MovieNotFoundException(movie)
      } else {
        item
      }
    } else {
      val items = dao.load(KEY_NAME_SIMPLIFIED, simpleName(movie.name, movie.year))
      if (items.isEmpty) {
        throw new MovieNotFoundException(movie)
      } else {
        items.iterator().next()
      }
    }
  }
  
  protected def simpleName(name: String, year: Long) = name.replace("(a |the |, a|, the|,|\\.|\\s|'|\"|:|-|!|#|)", "") + year

}