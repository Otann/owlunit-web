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

  implicit object iiDao extends FactoryMaker[IiDao](IiDaoConfig.dao)

  /**
   * objects in Scala are lazily created.  The init()
   * method creates a List of all the objects.  This
   * results in all the objects getting initialized and
   * registering their types with the dependency injector
   */
  def init(test: Boolean = false) {
    List(
      iiDao
    )
  }

}