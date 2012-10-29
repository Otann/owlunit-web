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
 *         Owls Proprietary
 *
 *         marker trait for sending items to UI
 */

trait IiTag {

  protected def iiId: String
  protected def iiType: String
  protected def iiName: String
  protected def iiUrl = "/%s/%s" format (iiType, iiId)
  protected def iiOwned = User.currentUser.map(_.hasItem(iiId)).openOr(false)

  def toJSON: JValue =(
    ("iiId" -> iiId)
      ~ ("iiName" -> iiName)
      ~ ("iiUrl" -> iiUrl)
      ~ ("iiType" -> iiType)
      ~ ("iiOwned" -> iiOwned)
    )

  def snippet = ".ii *" #> (iiType match {
    case "keyword" => <i class="icon-tag icon-white"></i> ++ Text(" " + iiName)
    case _ => Text(iiName)
    }) &
    ".ii [data-ii-id]" #> iiId &
    ".ii [data-ii-type]" #> iiType &
    ".ii [data-ii-name]" #> iiName &
    ".ii [data-ii-url]" #> iiUrl &
    ".ii [data-ii-owned]" #> iiOwned &
    ".ii [href]" #> iiUrl &
    ".ii [class+]" #> (if (iiOwned) "highlighted" else "")  //TODO why not working?

  def hrefSnippet = snippet & ".ii [href]" #> iiUrl

}

object IiTag extends AppHelpers with Loggable {
  implicit val formats = DefaultFormats

  case class IiTagCase(iiType: String, iiId: String, iiName: String) extends IiTag

  def apply(iiType: String, iiId: String, iiName: String) = IiTagCase(iiType, iiId, iiName)

  def unapply(tag: IiTag) = Some(tag.iiType, tag.iiId, tag.iiName)

  def fromJSON(json: JValue): Box[IiTag] = tryo { json.extract[IiTagCase] }

}