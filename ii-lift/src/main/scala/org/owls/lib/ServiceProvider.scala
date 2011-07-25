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

  val testMode = true

  val service = new Inject(buildOne _) {}

  def buildOne : MovieService = {
    if (testMode) {
      MockService
    } else {
      val cluster = HFactory.getOrCreateCluster("Local Cluster", "localhost:9160")
      val keyspace = HFactory.createKeyspace("InformationItems", cluster)
      new MovieService(keyspace)
    }
  }
}