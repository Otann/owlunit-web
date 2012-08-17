import bootstrap.liftweb.Boot
import com.owlunit.web.config.{IiDaoConfig, DependencyFactory}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 *
 *         Console helper
 */


object CH {

  val b: Boot = new Boot()

  def i() {
    b.boot()
  }

  def s() {
    IiDaoConfig.shutdown()
  }

}