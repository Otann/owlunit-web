package com.owlunit.service.cinema

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class CinemaException(reason: String,  ex: Throwable) extends RuntimeException {

  def this(reason: String) = this(reason, null)
}

class NotFoundException(item: CinemaItem, ex: Throwable)
  extends CinemaException ("Item with(%d) was not found" format item.id) {

  def this(item: CinemaItem) = this(item, null)
}

class MisusedException(item: CinemaItem, ex: Throwable)
  extends CinemaException ("You are using item(%d) with wrong service" format item.id) {

  def this(item: CinemaItem) = this(item, null)
}