package org.owls.lib

import com.manymonkeys.service.cinema.MovieService
import mock.MockService
import net.liftweb.util.SimpleInjector
import me.prettyprint.hector.api.factory.HFactory
import com.manymonkeys.core.ii.InformationItemDao

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */

object ServiceProvider extends SimpleInjector {

  val mockMode = false

  val service = new Inject(buildOne _) {}

  def buildOne : MovieService = {
    if (mockMode) {
      MockService
    } else {
      val cluster = HFactory.getOrCreateCluster("Test Cluster", "192.168.1.41:9160")
      val keyspace = HFactory.createKeyspace("InformationItems", cluster)
      new MovieService(keyspace)
    }
  }
}