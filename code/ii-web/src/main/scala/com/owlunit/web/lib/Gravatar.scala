package com.owlunit.web.lib

import java.io.UnsupportedEncodingException
import java.security.{MessageDigest, NoSuchAlgorithmException}
import net.liftweb.util.Helpers
import xml.NodeSeq
import net.liftweb.common.Loggable
import net.liftweb.http.Factory

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object Gravatar extends Factory with Loggable {

  val defaultRating = new FactoryMaker[String]("G") {}
  val defaultSize   = new FactoryMaker[Int](100) {}
  val defaultImage  = new FactoryMaker[String]("") {}

  /**
   * @param email The email address of the recipient
   * @param size The square size of the output gravatar
   * @param rating The rating of the Gravater, the default is G
   * @param default The default image to return if none exists for the given email
   */
  def imageUrl( email: String,
                size: Int = defaultSize.vend,
                rating: String = defaultRating.vend,
                default: String = defaultImage.vend
                ): String = {
    val url = "http://www.gravatar.com/avatar/%s?s=%s&r=%s" format (getMD5(email), size.toString, rating)
    if (default.length > 0) "%s&d=%s" format (url, Helpers.urlEncode(default)) else url
  }

  def imgTag( email: String,
              size: Int = defaultSize.vend,
              rating: String = defaultRating.vend,
              default: String = defaultImage.vend
              ): NodeSeq = {
    <img src={imageUrl(email, size, rating, default)}></img>
  }

  def signUpUrl(email: String): String =
    "http://en.gravatar.com/site/signup/%s".format(Helpers.urlEncode(email))

  private def getMD5(message: String): String = {
    val md: MessageDigest = MessageDigest.getInstance("MD5")
    val bytes = message.getBytes("CP1252")

    try {
      BigInt(1, md.digest(bytes)).toString(16)
    } catch {
      case a: NoSuchAlgorithmException => logger.error("[Gravater] No Algorithm.", a); ""
      case x: UnsupportedEncodingException => logger.warn("[Gravater] Unsupported Encoding.", x); ""
      case _ => logger.warn("[Gravater] Unknown error."); ""
    }
  }
}
