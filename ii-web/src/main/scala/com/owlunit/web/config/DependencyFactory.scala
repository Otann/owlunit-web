package com.owlunit.web.config

import net.liftweb._
import http._
import util._
import common._
import java.util.Date
import com.owlunit.core.ii.mutable.{Recommender, IiDao}

object DependencyFactory extends Factory {

  val fbApiKey = "327849237274882"
  val fbSecret = "f9e26660713c325586a957457e927b8d"

  val localMode = true // sys.env.getOrElse("OWL_DEPLOY_LOCAL", "false").toBoolean

  def shutdown() { dao.shutdown() }
  private val dao:IiDao = {
    if (localMode)
      IiDao.local("/Users/anton/Dev/Owls/data")
    else
      IiDao.remote("http://04e118aa4.hosted.neo4j.org:7034/db/data/", "a9786d4e8", "b72321c25")
  }

  implicit object iiDao extends FactoryMaker[IiDao](dao)

//  implicit object cinemaService extends FactoryMaker[CinemaService](CinemaService(DependencyFactory.inject[IiDao].open_!))

  /**
   * objects in Scala are lazily created.  The init()
   * method creates a List of all the objects.  This
   * results in all the objects getting initialized and
   * registering their types with the dependency injector
   */
  def init() {
    List(
      iiDao
    )
  }

}