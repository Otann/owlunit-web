package com.owlunit.web.lib

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiMeta {

  val Footprint = this.getClass.getName + ".MongoId"
  val Name  = this.getClass.getName + ".Name"

}

trait IiMovieMeta extends IiMeta {

  val Year  = this.getClass.getName + ".Year"

}