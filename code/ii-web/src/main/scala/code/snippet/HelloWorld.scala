package code 
package snippet 

import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import net.liftweb.common._
import java.util.Date
import code.lib._
import Helpers._
import com.owlunit.core.ii.IiDao
import com.owlunit.service.cinema.impl.KeywordServiceImpl
import com.owlunit.service.cinema.KeywordServiceImpl

class HelloWorld {

  lazy val date: Box[Date] = DependencyFactory.inject[Date] // inject the date
  
  lazy val keywordService = DependencyFactory.inject[KeywordServiceImpl].open_!

  lazy val keywordServiceBox = DependencyFactory.inject[KeywordServiceImpl]

  // replace the contents of the element with id "time" with the date
  def howdy = "#time *" #> Full("Robot")
  
  def test = "#test *" #> {
    ""
  }

  /*
   lazy val date: Date = DependencyFactory.time.vend // create the date via factory

   def howdy = "#time *" #> date.toString
   */
}

