package com.owlunit.core.ii.immutable

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.rest.graphdb.RestGraphDatabase
import org.neo4j.kernel.EmbeddedGraphDatabase

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait IiDao {

  def createIi: Ii
  def deleteIi(item: Ii)

  def load(id: Long): Ii
  def load(key: String, value: String): Seq[Ii]
  def search(key: String, queue: String): Seq[Ii]

  def setMeta(item: Ii, key: String, value: String): Ii
  def setMetaUnindexed(item: Ii, key: String, value: String): Ii
  def removeMeta(item: Ii, key: String): Ii

  def setComponentWeight(item: Ii, component: Ii, weight: Double): Ii
  def removeComponent(item: Ii, component: Ii): Ii

  def loadMeta(item: Ii): Ii
  def loadComponents(item: Ii): Ii
  def loadParents(item: Ii): Ii

  def getIndirectComponents(item: Ii): Map[Ii, Double]
  def recommender: Recommender //TODO for the lack of better ideas =(

  def init()
  def shutdown()
}

object IiDao {

  val DefaultDepth = 3

  def apply(graph: GraphDatabaseService, depth: Int = DefaultDepth): IiDao = new impl.NeoIiDao(graph, depth)

  def local(path: String, depth: Int = DefaultDepth): IiDao = apply(
    new EmbeddedGraphDatabase(path),
    depth
  )

  def remote(url: String, login: String, password: String, depth: Int = DefaultDepth): IiDao = apply(
    new RestGraphDatabase(url, login, password),
    depth
  )


}
