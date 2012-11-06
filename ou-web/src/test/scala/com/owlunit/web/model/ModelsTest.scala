package com.owlunit.web.model

import org.specs2.mutable.Specification
import net.liftweb.util.Props
import net.liftweb.common.Loggable
import com.owlunit.web.config.{IiDaoConfig, MongoConfig}
import scala.sys.process._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class ModelsTest extends Specification with ModelHelper with Loggable {

  step {

    val neoDbPath = Props.get("owlunit.neo4j.path", "/tmp/none")
    Seq("rm", "-rf", neoDbPath).!! // remove neo folder
    logger.debug("neo path was %s" format neoDbPath)

    val mongoDb = Props.get("owlunit.mongo.db", "none")
    Seq("mongo", mongoDb, "--eval", "'db.dropDatabase();'").!! // drop mongo db
    logger.debug("mongo db was %s" format mongoDb)

    MongoConfig.init()
    IiDaoConfig.init()
  }

  "User" should {
    "be able to save/load" in {
      val user = createRandomUser.save
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
      val movie = createRandomMovie.save
      user.addTag(movie).save
      User.find(user.id.is).open_!.movies.length mustEqual 1
    }
    "be able to add keyword" in {
      val user = loadRandomUser
      user.addTag(loadRandomKeyword).save
      User.find(user.id.is).open_!.keywords.length mustEqual 1
    }
    "be able to add person" in {
      val user = loadRandomUser
      val person = loadRandomPerson
      user.addTag(person).save
      User.find(user.id.is).open_!.persons.length mustEqual 1
    }
    "add movie, keyword and person" in {
      val user = loadRandomUser
      User.find(user.id.is).open_!.addTag(loadRandomMovie).save
      User.find(user.id.is).open_!.addTag(loadRandomKeyword).save
      User.find(user.id.is).open_!.addTag(loadRandomPerson).save
      User.find(user.id.is).open_!.ii.items.size mustEqual 3
    }
    "fails on web" in {
      logger.info("Failed test begind")

      // create fresh items
      val userId = loadRandomUser.id.is
      val movieId = loadRandomMovie.id.is
      val keywordId = loadRandomKeyword.id.is

      // make keyword used
      Movie.find(movieId).open_!.addKeyword(Keyword.find(keywordId).open_!).save

      // make user used
      User.find(userId).open_!.addTag(loadRandomKeyword).save

      // Perform add
      User.find(userId).open_!.addTag(Keyword.find(keywordId).open_!).save
      logger.info("Failed test end")

      User.find(userId).open_!.keywords.length mustEqual 2
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
      Person.createRecord.firstName("Johny").save
      val id = Person.createRecord.firstName("Johny").save.id.is
      Person.find(id).isDefined must beTrue
    }
  }

  step {

    IiDaoConfig.dao.shutdown()

  }

}