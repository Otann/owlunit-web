package com.manymonkeys.research.service.auth.impl

import com.manymonkeys.research.service.utils.IiDaoAccess
import com.manymonkeys.model.auth.User
import com.manymonkeys.core.ii.Ii
import com.manymonkeys.research.service.auth.impl.UserServiceImpl._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */


trait UserConverter extends IiDaoAccess {

  implicit def userToIi(user: User): Ii = {
    if (user.uuid != null) {
      val item = dao.load(user.uuid)
      if (item == null) {
        throw new NotFoundException(user.login)
      } else {
        item
      }
    } else {
      val items = dao.load(KEY_LOGIN, user.login)
      if (items.isEmpty) {
        throw new NotFoundException(user.login)
      } else {
        items.iterator().next()
      }
    }
  }

}