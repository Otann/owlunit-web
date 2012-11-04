package com.owlunit.web.snippet

import net.liftweb.http._
import js.{JsCmd, JsCmds}
import net.liftweb.common.Full
import com.owlunit.web.model.{Keyword, Person, Movie}
import com.owlunit.web.lib.ui.BootstrapScreen
import xml.NodeSeq
import com.owlunit.web.lib.AppHelpers

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 */
object ScreenCreateMovie extends BootstrapScreen with AppHelpers {
  override def screenTop = Full(<h3>Create Movie</h3>)
  override def cancelCaption = ""

  addFields(() => objectVar.is.createFields )
  object objectVar extends ScreenVar(Movie.createRecord)

  protected def finish() { objectVar.is.save }
  override protected def doFinish() = {
    val notice = if (validate.isEmpty)
      JsLog(objectVar.is.toString() + " created")
    else
      JsCmds.Noop
    super.doFinish() & notice
  }
}

object ScreenCreateKeyword extends BootstrapScreen with AppHelpers {
  override def screenTop = Full(<h3>Create Keyword</h3>)
  override def cancelCaption = ""

  addFields(() => objectVar.is.createFields )
  object objectVar extends ScreenVar(Keyword.createRecord)

  protected def finish() { objectVar.is.save }
  override protected def doFinish() = {
    val notice = if (validate.isEmpty)
      JsLog(objectVar.is.toString() + " created")
    else
      JsCmds.Noop
    super.doFinish() & notice
  }

}