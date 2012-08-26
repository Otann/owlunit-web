package com.owlunit.web.config

import com.owlunit.core.ii.mutable.IiDao
import java.net.InetAddress

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object IiDaoConfig {

  def isNestFrontend = InetAddress.getLocalHost.getHostName == "nest-frontend"
  def localPath = if (isNestFrontend) "/mnt/data/owlunit/neo4j/" else "/usr/local/var/neo4j/"

  val localMode = true // sys.env.getOrElse("OWL_DEPLOY_LOCAL", "false").toBoolean

  def init() { dao.init() } // creates shutdown hook
  def shutdown() { dao.shutdown() }

  val dao:IiDao = {
    if (localMode)
      IiDao.local(localPath)
    else
      IiDao.remote("http://04e118aa4.hosted.neo4j.org:7034/db/data/", "a9786d4e8", "b72321c25")
  }

}
