package com.owlunit.web.config

import net.liftweb.mongodb.{MongoDB, DefaultMongoIdentifier}
import com.mongodb.{ServerAddress, Mongo}
import net.liftweb.util.Props


/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

object MongoConfig {

  def init() {

    // On Mac OS X: mongod --dbpath=/data/db/
    val server = new ServerAddress(
       Props.get("owlunit.mongo.host", "127.0.0.1"),
       Props.getInt("owlunit.mongo.port", 27017)
    )

    def dbName = Props.get("owlunit.mongo.db", "owl")

    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo(server), dbName)

  }

}