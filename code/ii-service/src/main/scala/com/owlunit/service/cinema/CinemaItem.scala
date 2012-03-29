package com.owlunit.service.cinema

import com.owlunit.core.ii.Ii

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class CinemaItem (val id: Long) {

  override def hashCode() = id.hashCode()

  override def equals(p: Any) = p.isInstanceOf[CinemaItem] && p.asInstanceOf[CinemaItem].id == id

  override def toString = "%s(%d)" format  (this.getClass.getSimpleName, id)

}