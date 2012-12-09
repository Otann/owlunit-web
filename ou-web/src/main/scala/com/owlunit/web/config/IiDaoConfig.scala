package com.owlunit.web.config

import com.owlunit.core.ii.mutable.IiDao
import net.liftweb.util.Props

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */


object IiDaoConfig {

  def localPath = Props.get("owlunit.neo4j.path", "/mnt/data/owlunit/neo4j/")

  def isRemote  = Props.get("owlunit.neo4j.remote",   "false").toBoolean
  def host      = Props.get("owlunit.neo4j.host",     "localhost")
  def username  = Props.get("owlunit.neo4j.username", "root")
  def password  = Props.get("owlunit.neo4j.password", "password")

//  val localMode = sys.env.getOrElse("OWL_DEPLOY_LOCAL", "false").toBoolean
//  IiDao.remote("http://04e118aa4.hosted.neo4j.org:7034/db/data/", "a9786d4e8", "b72321c25")

  lazy val dao = if (isRemote) IiDao.remote(host, username, password) else IiDao.local(localPath)

  def init() { dao.init() }
  def shutdown() { dao.shutdown() }

}
