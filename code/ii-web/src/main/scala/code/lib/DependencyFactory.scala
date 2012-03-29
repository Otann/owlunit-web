package code
package lib

import net.liftweb._
import http._
import util._
import common._
import java.util.Date
import com.owlunit.core.ii.IiDao
import com.owlunit.service.cinema.impl.{KeywordServiceImpl, PersonServiceImpl, MovieServiceImpl}
import com.owlunit.service.cinema.{PersonServiceImpl, MovieServiceImpl, KeywordServiceImpl}

/**
 * A factory for generating new instances of Date.  You can create
 * factories for each kind of thing you want to vend in your application.
 * An example is a payment gateway.  You can change the default implementation,
 * or override the default implementation on a session, request or current call
 * stack basis.
 */
object DependencyFactory extends Factory {


  val Neo4jPath = "/Users/anton/Dev/Owls/data"

  implicit object time extends FactoryMaker(Helpers.now _)

  implicit object iiDao extends FactoryMaker(IiDao(Neo4jPath))

  implicit object keywordService  extends FactoryMaker(KeywordServiceImpl(DependencyFactory.iiDao.vend))
  implicit object personService   extends FactoryMaker(PersonServiceImpl(DependencyFactory.iiDao.vend))
  implicit object movieService    extends FactoryMaker(MovieServiceImpl(DependencyFactory.iiDao.vend))

  /**
   * objects in Scala are lazily created.  The init()
   * method creates a List of all the objects.  This
   * results in all the objects getting initialized and
   * registering their types with the dependency injector
   */
  private def init() {
    List(
      time,
      iiDao,
      keywordService,
      personService,
      movieService
    )
  }

  init()
}


/**
 * Examples of changing the implementation
 */
sealed abstract class Changer {

  def changeDefaultImplementation() {
    DependencyFactory.time.default.set(() => new Date())
  }

  def changeSessionImplementation() {
    DependencyFactory.time.session.set(() => new Date())
  }

  def changeRequestImplementation() {
    DependencyFactory.time.request.set(() => new Date())
  }

  def changeJustForCall(d: Date) {
    DependencyFactory.time.doWith(d) {
      // perform some calculations here
    }
  }

}

