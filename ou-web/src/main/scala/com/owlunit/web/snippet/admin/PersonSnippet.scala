package com.owlunit.web.snippet.admin

import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JE._
import js.JsCmd
import js.JsCmds._
import xml.NodeSeq
import com.owlunit.web.model.Person
import net.liftweb.common._
import net.liftweb.record.Field

/**
* @author Anton Chebotaev
*         Owls Proprietary
*/

object PersonSnippet {

  def current: Box[Person] = for {
    id <- S.param("id") ?~ "You must provide an id"
    movie <- Person.find(id)
  } yield { movie }

  def render = "person-data=iiid [itemid]" #> current.map(_.ii.id.toString) &
    "person-data=name *" #> current.map(_.fullName) &
    "person-data=photo [src]" #> current.map(_.photoUrl.is)

}