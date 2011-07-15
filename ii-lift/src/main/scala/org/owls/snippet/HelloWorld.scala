package org.owls.snippet

import scala.xml.{NodeSeq}
import net.liftweb.util.Helpers._
import com.manymonkeys.service.cinema.MovieService
import org.owls.lib.ServiceProvider
import net.liftweb.common.Box

class HelloWorld {
  def render = "*" #> <strong>hello world!</strong>
  val service : MovieService = ServiceProvider.service.vend
}