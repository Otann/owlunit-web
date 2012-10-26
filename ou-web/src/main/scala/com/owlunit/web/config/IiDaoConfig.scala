package com.owlunit.web.config

import com.owlunit.core.ii.mutable.IiDao
import java.net.InetAddress
import net.liftweb.util.{Props, Helpers}
import net.liftweb.common.Full

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object IiDaoConfig {

  def localPath = Props.get("owlunit.neo4j.path", "/dev/null") //TODO: /dev/null is bad

//  val localMode = sys.env.getOrElse("OWL_DEPLOY_LOCAL", "false").toBoolean
//  IiDao.remote("http://04e118aa4.hosted.neo4j.org:7034/db/data/", "a9786d4e8", "b72321c25")

  val dao = IiDao.local(localPath)

  def init() { dao.init() }
  def shutdown() { dao.shutdown() }

}
