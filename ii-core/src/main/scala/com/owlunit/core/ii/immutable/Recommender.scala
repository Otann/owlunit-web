package com.owlunit.core.ii.immutable

/**
  * @author Anton Chebotaev
  *         Owls Proprietary
  */

trait Recommender {

  def defaultLimit = 100

  def compare(a: Ii, b: Ii): Double
  def getSimilar(a: Ii, key: String): Map[Ii, Double]
  def getSimilar(p: Map[Ii, Double], key: String, limit: Int = defaultLimit): Map[Ii, Double]

}
