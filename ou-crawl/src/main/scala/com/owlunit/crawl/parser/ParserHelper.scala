package com.owlunit.crawl.parser

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


trait ParserHelper {

  def capitalizeKeyword(k: String) = k.split('-').map(capitalizeWord).mkString(" ")
  def capitalizeWord(s: String) = s(0).toUpper + s.substring(1, s.length).toLowerCase

  protected def simplifyName(args: Any*): String = args.mkString.toLowerCase
    .replaceAll(", the|, a|the |a |", "")
    .replaceAll("[\\W&&\\D]", "")

  def stripQuotes(name: String) = {
    if (name.startsWith("\"") && name.endsWith("\""))
      name.substring(1, name.length - 1)
    else
      name
  }

}
