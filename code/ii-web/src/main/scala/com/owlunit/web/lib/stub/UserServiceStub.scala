package com.owlunit.web.lib.stub

import com.owlunit.service.auth.{User, UserService}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait UserServiceStub extends UserService {

  val user = new User(0, "aeiou")

  def createUser(externalId: String) = user

}