package com.owlunit.web

import config.{IiDaoConfig, DependencyFactory, MongoConfig}
import model.{Person, Keyword}
import org.specs2.mutable.Specification
import com.owlunit.core.ii.mutable.IiDao
import com.owlunit.core.ii.NotFoundException
import bootstrap.liftweb.Boot
import net.liftweb.util.Props
import net.liftweb.common.Loggable

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class ModelsTest extends Specification with Loggable {

  step {
    logger.debug(Props.get("neo4j.path", "undef"))
    MongoConfig.init()
    IiDaoConfig.init()
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