package com.owlunit.web.snippet.admin

import net.liftweb.http._
import js.JsCmds
import net.liftweb.common.Full
import com.owlunit.web.model.{Person, Movie}
import com.owlunit.web.lib.ui.BootstrapScreen

/**
* @author Anton Chebotaev
*         Owls Proprietary
*/

object CreateMovieScreen extends BootstrapScreen with AdminHelper {

  override def screenTop = Full(<h3>Create Movie</h3>)
  override def cancelCaption = ""

  object movieVar extends ScreenVar(Movie.createRecord)
  addFields(() => movieVar.is.createFields )

  protected def finish() {
    movieVar.is.save
  }

  override protected def doFinish() = {
    val notice = if (validate.isEmpty)
      jsAlert(NoticeType.Notice, <span>Movie { movieVar.is.toString() } created</span>)
    else
      JsCmds.Noop
    super.doFinish() & notice
  }

}

object CreatePersonScreen extends BootstrapScreen with AdminHelper {

  override def screenTop = Full(<h3>Create Person</h3>)
  override def cancelCaption = ""

  object personVar extends ScreenVar(Person.createRecord)
  addFields(() => personVar.is.createFields )

  protected def finish() {
    personVar.is.save
    S.notice(<span>Person <strong>{ personVar.is.toString() }</strong> crated!</span>)
    S.redirectTo(personVar.is.tagUrl)
  }

//  override protected def doFinish() = {
//    val notice = if (validate.isEmpty) {
//      val oldPerson = personVar.is.toString()
//      personVar(Person.createRecord)
//      jsAlert(NoticeType.Notice, <span>Person { oldPerson } created</span>)
//    } else
//      JsCmds.Noop
//    super.doFinish() & notice
//  }

}
