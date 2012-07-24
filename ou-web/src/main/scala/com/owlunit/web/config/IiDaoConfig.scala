package com.owlunit.web.config

import com.owlunit.core.ii.mutable.IiDao

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


object IiDaoConfig {

  val localMode = true // sys.env.getOrElse("OWL_DEPLOY_LOCAL", "false").toBoolean

  def init() { dao.init() } // creates shutdown hook
  def shutdown() { dao.shutdown() }

  val dao:IiDao = {
    if (localMode)
      IiDao.local("/usr/local/var/neo4j/")
    else
      IiDao.remote("http://04e118aa4.hosted.neo4j.org:7034/db/data/", "a9786d4e8", "b72321c25")
  }

}
