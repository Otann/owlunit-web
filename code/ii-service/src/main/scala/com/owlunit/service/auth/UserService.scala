package com.owlunit.service.auth

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class User(id: Long, externalId: String) {}

trait UserService {

  def createUser(externalId: String): User
  

}