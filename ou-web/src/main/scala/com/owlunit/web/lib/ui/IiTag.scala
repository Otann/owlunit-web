package com.owlunit.web.lib.ui

import net.liftweb.util.Helpers._
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.DefaultFormats
import com.owlunit.web.lib.AppHelpers
import net.liftweb.common.{Loggable, Box}
import com.owlunit.web.model.User
import xml.Text

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 *
 *         marker trait for sending items to UI
 */

trait IiTag {

  def kind: String
  def name: String
  def objectId: String

  protected def url = "/%s/%s" format (kind, objectId)
  protected def isOwned = User.currentUser.map(_.hasItem(objectId)).openOr(false)

  private def iconTag(name: String) = <i class={ "icon-" + name }></i>
  private def icon = kind match {
    case "keyword"  => iconTag("lemon")
    case "movie"    => iconTag("film")
    case "person"   => iconTag("user")
    case _          => Text("")
  }

  def toJSON: JValue =(
    ("objectId" -> objectId)
      ~ ("name" -> name)
      ~ ("url" -> url)
      ~ ("kind" -> kind)
      ~ ("isOwned" -> isOwned)
    )

  def snippet = ".ii *"   #> (icon ++ Text(name)) &
    ".ii [data-objectid]" #> objectId &
    ".ii [data-kind]"     #> kind &
    ".ii [data-name]"     #> name &
    ".ii [data-url]"      #> url &
    ".ii [data-isowned]"  #> isOwned &
    ".ii [href]"          #> url &
    ".ii [class+]"        #> (if (isOwned) "owned" else "")

}

object IiTag extends AppHelpers with Loggable {
  implicit val formats = DefaultFormats

  case class IiTagCase(kind: String, objectId: String, name: String) extends IiTag

  def apply(kind: String, objectId: String, name: String) = IiTagCase(kind, objectId, name)

  def unapply(tag: IiTag) = Some(tag.kind, tag.objectId, tag.name)

  def fromJSON(json: JValue): Box[IiTag] = tryo { json.extract[IiTagCase] }

}