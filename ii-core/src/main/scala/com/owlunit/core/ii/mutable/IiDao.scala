package com.owlunit.core.ii.mutable

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase
import org.neo4j.rest.graphdb.RestGraphDatabase

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiDao extends Recommender {

  def init()
  def shutdown()

  def create: Ii

  def load(id: Long): Ii
  def load(key: String, value: String): Seq[Ii]
  def search(key: String, queue: String): Seq[Ii]

  def indirectComponents(item: Ii, depth: Int): Map[Ii, Double]
  def within(item: Ii): Map[Ii, Double]

}

object IiDao {

  def apply(graph: GraphDatabaseService): IiDao = new impl.NeoIiDao(graph)

  def local(path: String): IiDao = apply(
    new EmbeddedGraphDatabase(path)
  )

  def remote(url: String, login: String, password: String): IiDao = apply(
    new RestGraphDatabase(url, login, password)
  )

}
