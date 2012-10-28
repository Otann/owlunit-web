package com.owlunit.web.lib.ui

import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JValue

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         marker trait for sending items to UI
 */

trait IiTag {

  protected def tagId: String
  protected def tagType: String
  protected def tagCaption: String
  protected def tagUrl: String

  def toJSON: JValue = (("id" -> tagId) ~ ("caption" -> tagCaption) ~ ("url" -> tagUrl) ~ ("type" -> tagType))

  def snippet = ".ii *" #> tagCaption &
    ".ii [data-id]" #> tagId &
    ".ii [data-type]" #> tagType &
    ".ii [data-caption]" #> tagCaption &
    ".ii [data-url]" #> tagUrl

  def hrefSnippet = snippet & ".ii [href]" #> tagUrl

}
