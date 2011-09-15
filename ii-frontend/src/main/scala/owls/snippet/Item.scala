package owls.snippet

import net.liftweb.util.Helpers._
import org.owls.lib.ServiceProvider
import net.liftweb.http.S
import com.manymonkeys.service.cinema.MovieService
import com.manymonkeys.core.ii.Ii
import net.liftweb.util.CssSel
import scala.collection.JavaConversions._
import java.util.{Collections, UUID}
import xml.{Text, NodeSeq}
import net.liftweb.textile._

// this must be gone

class Item {

  val service : MovieService = ServiceProvider.service.vend
//
//  def render =
//    ".name *" #> item.getMeta(TagService.NAME) &
//    ".description" #> tryo(TextileParser.toHtml(item.getMeta(MovieService.TAGLINES))) &
//    ".param *" #> S.param("id").getOrElse("no parameter was passed") &
//    ".components *" #> components(item)
//
//  def components(item: Ii) =
//    ".ii-tag *" #> item.getComponents.map({case (i, w) => (i, w.doubleValue())}).toSeq.sortWith(_._2 > _._2).map({case (i, w) => tag(i, w)})
//
//  def tag(item: Ii, weight: Double) : CssSel =
//    ".name [href]" #> ("/item/" + item.getUUID.toString) &
//    ".name *" #> item.getMeta(TagService.NAME) &
//    ".weight *" #> "%1.1f".format(weight)

}