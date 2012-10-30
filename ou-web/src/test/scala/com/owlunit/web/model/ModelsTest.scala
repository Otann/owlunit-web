package com.owlunit.web.model

import org.specs2.mutable.Specification
import net.liftweb.util.Props
import net.liftweb.common.Loggable
import com.owlunit.web.config.{IiDaoConfig, MongoConfig}
import java.io.File

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class ModelsTest extends Specification with ModelHelper with Loggable {

  var dbPath = ""

  step {
    System.setProperty("run.mode", "test")

    dbPath = Props.get("owlunit.neo4j.path", "target/neo4j")
    logger.debug("db path: %s" format dbPath)
    (new File(dbPath)).delete()

    MongoConfig.init()
    IiDaoConfig.init()
  }

  "User" should {
    "be able to save/load" in {
      val user = createRandomUser.save
      logger.debug("User's id: %s" format user.id.is)
      User.find(user.id.is).isDefined must beTrue
    }
    "be created with non initialized ii" in {
      val user = createRandomUser
      user.ii.id mustEqual 0
    }
    "have initialized ii" in {
      val user = createRandomUser.save
      User.find(user.id.is).open_!.ii.id mustNotEqual 0
    }
    "be created with empty movies" in {
      val user = createRandomUser.save
      User.find(user.id.is).open_!.movies must beEmpty
    }
    "be created with empty keywords" in {
      val user = createRandomUser.save
      User.find(user.id.is).open_!.keywords must beEmpty
    }
    "be created with empty persons" in {
      val user = createRandomUser.save
      User.find(user.id.is).open_!.persons must beEmpty
    }
    "be able to add movie" in {
      val user = createRandomUser.save
      user.addTag(createRandomKeyword.save).save
      val movie = createRandomMovie.save
      user.addTag(movie).save
      User.find(user.id.is).open_!.movies.length mustEqual 1
    }
    "be able to add keyword" in {
      val user = loadRandomUser
      user.addTag(loadRandomKeyword).save
      logger.debug("Keyword before load: %s" format user.keywords)
      User.find(user.id.is).open_!.keywords.length mustEqual 1
    }
    "be able to add person" in {
      val user = createRandomUser.save
      val person = createRandomPerson.save
      user.addTag(person).save
      User.find(user.id.is).open_!.persons.length mustEqual 1
    }
  }

  "Keyword" should {
    "be able to save/load" in {
      val id = Keyword.createRecord.name("keyword").save.id.is
      Keyword.find(id).isDefined must beTrue
    }
  }

  "Person" should {
    "be able to save/load" in {
      val id = Person.createRecord.firstName("Johny").lastName("Doe").save.id.is
      Person.find(id).isDefined must beTrue
    }
  }

  step {

    IiDaoConfig.dao.shutdown()
    (new File(dbPath)).delete()

    System.setProperty("run.mode", "development")
  }

}