package com.owlunit.web.config

import net.liftweb.mongodb.{MongoDB, DefaultMongoIdentifier}
import net.liftweb.util.Props
import com.mongodb.{ServerAddress, Mongo}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object MongoConfig {

  def init() {
    val server = new ServerAddress(
       Props.get("mongo.host", "127.0.0.1"),
       Props.getInt("mongo.port", 27017)
    )

    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(server), "owl")
  }

}