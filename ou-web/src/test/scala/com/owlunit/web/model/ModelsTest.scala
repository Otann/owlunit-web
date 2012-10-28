package com.owlunit.web.model

import org.specs2.mutable.Specification
import net.liftweb.util.{Helpers, Props}
import net.liftweb.common.Loggable
import com.owlunit.web.config.{IiDaoConfig, MongoConfig}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class ModelsTest extends Specification with Loggable {

  def randomLong = Helpers.nextNum
  def randomString = Helpers.nextFuncName

  def createRandomUser = {
    val user = User.createRecord
    user.facebookId(randomLong)
    user.name(randomString)
    user.email(randomString)
    user
  }
  def createRandomMovie = {
    val movie = Movie.createRecord
    movie.name(randomString)
    movie.year(2000)
    movie
  }

  step {
    System.setProperty("run.mode", "test")
    MongoConfig.init()
    IiDaoConfig.init()
  }

  "User" should {
    "be able to save/load" in {
      val user = createRandomUser.save
      logger.debug("Newly created user = %s" format user)
      User.find(user.id.is) must not beEmpty
    }
    "be created with non 0 ii.id" in {
      val user = createRandomUser.save
      logger.debug("Newly created user = %s" format user)
      user.ii.id mustNotEqual 0
    }
    "be able to add movie" in {
      val user = createRandomUser.save
      val movie = createRandomMovie

      logger.debug("Newly created user = %s" format user)
      user.ii.id mustNotEqual 0
    }
  }

  "Keyword" should {
    "be able to save/load" in {
      val id = Keyword.createRecord.name("keyword").save.id.is
      Keyword.find(id) must not beEmpty
    }
  }

  "Person" should {
    "be able to save/load" in {
      val id = Person.createRecord.firstName("Johny").lastName("Doe").save.id.is
      Person.find(id) must not beEmpty
    }
  }

  step {
    IiDaoConfig.dao.shutdown()
  }

}