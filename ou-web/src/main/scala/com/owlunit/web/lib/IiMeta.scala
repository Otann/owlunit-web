package com.owlunit.web.lib

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiMeta {

  def baseMeta: String
  val Footprint = baseMeta + ".MongoId"
  val Name  = baseMeta + ".Name"

}

trait IiMovieMeta extends IiMeta {

  val SimpleName = baseMeta + ".SimpleName"

  val KeywordWeight = 1.0
  val ActorWeight = 5.0

}

trait IiPersonMeta extends IiMeta {

  val SimpleName = baseMeta + ".SimpleName"

}