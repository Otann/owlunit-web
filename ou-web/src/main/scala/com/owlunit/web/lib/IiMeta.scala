package com.owlunit.web.lib

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Keys for storing meta in ii backend
 */


trait IiMeta {

  def baseMeta: String
  def Footprint = baseMeta + ".MongoId"
  def Name      = baseMeta + ".Name"

}

trait IiMovieMeta extends IiMeta {

  val KeywordWeight       = 1.0

  val GeneralPersonWeight = 5.0
  val ActorWeight         = 5.0
  val DirectorWeight      = 5.0
  val ProducerWeight      = 5.0

}

trait IiPersonMeta extends IiMeta {

  def SimpleName = baseMeta + ".SimpleName"

}