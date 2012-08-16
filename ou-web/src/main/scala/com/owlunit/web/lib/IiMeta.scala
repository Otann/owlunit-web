package com.owlunit.web.lib

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiMeta {

  def baseMeta: String
  def Footprint = baseMeta + ".MongoId"
  def Name  = baseMeta + ".Name"

}

trait IiMovieMeta extends IiMeta {

  def SimpleName = baseMeta + ".SimpleName"

  val KeywordWeight = 1.0
  val GeneralPersonWeight = 5.0

}

trait IiPersonMeta extends IiMeta {

  def SimpleName = baseMeta + ".SimpleName"

}