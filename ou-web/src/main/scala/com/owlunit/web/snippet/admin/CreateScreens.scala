package com.owlunit.web.snippet.admin

import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JsCmds
import net.liftweb.common.{Logger, Full, Failure, Empty}
import com.owlunit.web.lib.{BootstrapScreen, AppHelpers}
import xml.{Text, NodeSeq}
import com.owlunit.web.model.{Person, User, Movie}

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
    S.redirectTo(personVar.is.url)
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
