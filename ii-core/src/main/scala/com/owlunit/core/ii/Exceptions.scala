package com.owlunit.core.ii

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class DAOException(reason: String, ex: Exception) extends RuntimeException(reason, ex) {

  def this(reason: String) = this(reason, null)
  def this() = this(null, null)

}

class NotFoundException(id: Long, ex: Exception) extends DAOException("Item was not found id=" + id, ex) { }