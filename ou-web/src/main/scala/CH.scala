import bootstrap.liftweb.Boot
import com.owlunit.web.config.DependencyFactory

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object CH { //Console helper

  val b: Boot = new Boot()

  def i() {
    b.boot()
  }

  def s() {
//    DependencyFactory.shutdown()
  }

}