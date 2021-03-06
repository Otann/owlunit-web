package com.owlunit.web.model.common

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         Defines metadata keys for Ii subsystem
 */

trait IiTagMeta {

  // Base prefix for global meta keys
  protected val metaGlobal = "ii.cinema"
  protected def metaGlobalObjectId   = metaGlobal + ".IiId"
  protected def metaGlobalName = metaGlobal + ".iiName"
  protected def metaGlobalType = metaGlobal + ".iiType"

  // Base prefix for local meta keys
  protected def metaBase = metaGlobal

}
