package com.owlunit.web.lib

import net.liftweb.util._
import Helpers._
import com.owlunit.service.cinema._
import xml.NodeSeq
import net.liftweb.common.Full

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object CinemaUtilities {

  def renderKeyword(keyword: KeywordIi) = <span class="ii label">{keyword.name}</span>

}