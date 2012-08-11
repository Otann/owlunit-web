package com.owlunit.web.model

import net.liftweb.http.js.JsObj
import net.liftweb.http.js.JE.JsObj
import net.liftweb.http.js.JsObj

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait IiTag {

  protected def tagId: String
  protected def tagCaption: String

  def toTagJSON: JsObj = JsObj(("id", tagId), ("caption", tagCaption))

}
