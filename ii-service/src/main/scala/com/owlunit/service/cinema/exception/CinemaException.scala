package com.owlunit.service.cinema.exception

import com.owlunit.service.cinema.CinemaIi

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class CinemaException(reason: String,  ex: Throwable) extends RuntimeException {
  def this(reason: String) = this(reason, null)
}

class NotFoundException(item: CinemaIi, ex: Throwable)
  extends CinemaException ("Item with(%d) was not found" format item.id) {
  def this(item: CinemaIi) = this(item, null)
}

class MisusedException(item: CinemaIi, ex: Throwable)
  extends CinemaException ("You are using item(%d) with wrong service" format item.id) {
  def this(item: CinemaIi) = this(item, null)
}