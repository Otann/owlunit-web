package com

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


class ScalaTest {

  def returnOption(): Option[String] = Option(null)

  def returnDouble(): Double = 1.0

  def returnMap(): Map[String, Double] = Map("1" -> 2)

  def returnSeq(): Seq[String] = "1" :: "2" :: "3" :: Nil

}