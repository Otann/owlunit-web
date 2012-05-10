package com.owlunit.core.ii.mutable

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait Ii {

  def id: Long

  def meta: Option[Map[String, String]]
  def items: Option[Map[Ii, Double]]

  def loadMeta: Ii
  def loadItems: Ii

  def save: Ii
  def delete()
  def setMeta(key: String, value: String): Ii
  def setItem(component: Ii, weight: Double): Ii

}