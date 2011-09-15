package org.owls.lib

import com.manymonkeys.service.cinema.MovieService
import net.liftweb.util.SimpleInjector
import me.prettyprint.hector.api.factory.HFactory
import com.manymonkeys.core.ii.IiDao
import com.manymonkeys.research.service.cinema.impl.MovieService.MovieServiceImpl
import com.manymonkeys.core.ii.impl.cassandra.CassandraIiDaoImpl
import com.manymonkeys.core.algo.impl.RecommenderPlainImpl

/**
 * Many Monkeys
 *
 * @author Anton Chebotaev
 */
object ServiceProvider extends SimpleInjector {

  val mockMode = false

  val service = new Inject(buildOne _) {}

  def buildOne : MovieService = {
    val cluster = HFactory.getOrCreateCluster("Test Cluster", "home.chebotaev.ru:9160")
    val keyspace = HFactory.createKeyspace("InformationItems", cluster)

    val dao: IiDao = new CassandraIiDaoImpl(keyspace)
    val recommender = new RecommenderPlainImpl()

    val movieService = new MovieServiceImpl()
    movieService.setDao(dao)
    movieService.setRecommender(recommender)

    movieService.asInstanceOf[MovieService]
  }
}