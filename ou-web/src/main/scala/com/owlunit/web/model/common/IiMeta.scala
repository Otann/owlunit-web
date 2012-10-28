package com.owlunit.web.model.common

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Defines metadata keys for Ii subsystem
 */

trait IiMeta {

  // Base prefix for global meta keys
  protected val metaGlobal = "ii.cinema"
  protected def metaObjectId = metaGlobal + ".IiId"

  // Base prefix for local meta keys
  protected def metaBase = metaGlobal



}
