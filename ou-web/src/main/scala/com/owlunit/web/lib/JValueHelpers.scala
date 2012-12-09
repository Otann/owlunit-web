package com.owlunit.web.lib

import net.liftweb.json._
import net.liftweb.common.{Full, Empty, Loggable, Box}
import net.liftweb.util.Helpers._

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */
trait JValueHelpers extends Loggable{

  def extract[T](value: JValue)(func: JValue => JValue): Box[T] =
    func(value) match {
      case JNothing => Empty
      case _        => Full(func(value).values.asInstanceOf[T])
    }

  def update[T](value: JValue)(extractor: JValue => JValue)(updater: T => Any) {
    extract[T](value)(extractor).map(updater)
  }

}
object JValueHelpers extends JValueHelpers