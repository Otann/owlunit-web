package com.owlunit.web.lib

import net.liftweb.http.js.JE.JsObj
import net.liftweb.http.js.JsObj

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         marker trait for sending items to UI
 */

trait IiTag {

  protected def tagId: String

  protected def tagCaption: String

  def toTagJSON: JsObj = JsObj(("id", tagId), ("caption", tagCaption))

}
