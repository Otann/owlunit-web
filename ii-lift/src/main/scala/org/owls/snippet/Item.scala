package org.owls.snippet

import scala.xml.{NodeSeq}
import net.liftweb.util.Helpers._
import org.owls.lib.ServiceProvider
import java.util.UUID
import net.liftweb.http.S
import com.manymonkeys.service.cinema.{TagService, MovieService}
import org.owls.lib.mock.MockItem
import com.manymonkeys.core.ii.InformationItem
import net.liftweb.util.CssSel
import scala.collection.JavaConversions._

class Item {

//  val service : MovieService = ServiceProvider.service.vend
  val item = MockItem //.loadByUUID(UUID.fromString(S.param("id").get)) //TODO: error prone

  def render =
    ".name *" #> <h3>{item.getMeta(TagService.NAME)}</h3> &
    ".param *" #> S.param("id").getOrElse("no parameter passed") &
    ".components *" #> components(item)

  def components(item: InformationItem) = ".ii-tag *" #> item.getComponents.map({case (i, w) => tag(i, w.doubleValue())})

  def tag(item: InformationItem, weight: Double) : CssSel =
    ".ii-name [href]" #> ("/item?id=" + item.getUUID.toString) &
    ".ii-name *" #> item.getMeta(TagService.NAME) &
    ".weight *" #> weight.toString

}