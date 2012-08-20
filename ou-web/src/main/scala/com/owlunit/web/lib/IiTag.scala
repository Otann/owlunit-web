package com.owlunit.web.lib

import net.liftweb.http.js.JE.JsObj
import net.liftweb.http.js.JsObj
import net.liftweb.util.Helpers._
import com.owlunit.web.model.User

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         marker trait for sending items to UI
 */

trait IiTag {

  protected def tagId: String
  protected def tagCaption: String
  protected def tagUrl: String //TODO(Anton): make more "lifty"

  def toTagJSON: JsObj = JsObj(("id", tagId), ("caption", tagCaption), ("url", tagUrl))

  def render =
    ".ii *" #> tagCaption &
    ".ii [data-id]" #> tagId &
    ".ii [data-caption]" #> tagCaption &
    ".ii [data-url]" #> tagUrl


}
