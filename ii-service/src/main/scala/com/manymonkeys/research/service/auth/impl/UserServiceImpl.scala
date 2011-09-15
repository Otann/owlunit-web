package com.manymonkeys.research.service.auth.impl

import com.manymonkeys.service.auth.UserService
import com.manymonkeys.model.auth.User
import com.manymonkeys.research.service.utils.IiDaoAccess
import com.manymonkeys.core.ii.Ii
import java.security.MessageDigest
import com.manymonkeys.model.cinema.Movie
import reflect.BeanProperty
import com.manymonkeys.research.service.cinema.impl.MovieService.MovieConverter
import UserServiceImpl._

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object UserServiceImpl {

  final val CREATOR_KEY   = classOf[UserService].getName
  final val CREATOR_VALUE = "#"

  final val KEY_LOGIN     = classOf[UserService].getName + ".LOGIN"
  final val KEY_PASSWORD  = classOf[UserService].getName + ".PASSWORD"
  final val SALT          = "Humpty Dumpty sat on a wall, Humpty Dumpty had a great fall"

}

class UserServiceImpl extends UserService
                      with IiDaoAccess
                      with UserConverter
                      with MovieConverter {

  @BeanProperty
  var defaultFollowerWeight: Double = 1

  @BeanProperty
  var defaultLikeWeight: Double = 1

  @BeanProperty
  var defaultRateMultiplicator: Double = 1.0

  private implicit def iiToUser(item: Ii): User = {
    val meta = itemWithMeta(item)
    User(login = meta.getMeta(KEY_LOGIN), uuid = meta.getUUID)
  }

  def createUser(user: User) = {
    val item = dao.createInformationItem()
    //TODO Ilya Pimenov - validate login full to be alphanumeric
    dao.setUnindexedMeta(item, KEY_LOGIN, user.login)
  }

  def getUser(login: String) = {
    val items = dao.load(KEY_LOGIN, login)
    if (items.isEmpty) {
      throw new NotFoundException(login)
    } else {
      items.iterator().next()
    }
  }

  def setPassword(user: User, password: String) = dao.setMeta(user, KEY_PASSWORD, md5(password + SALT))

  def checkPassword(user: User, password: String) = md5(password + SALT) == itemWithMeta(user).getMeta(KEY_PASSWORD)

  def follow(follower: User, followed: User) = dao.setComponentWeight(follower, followed, defaultFollowerWeight)

  def unfollow(follower: User, followed: User) = dao.setComponentWeight(follower, followed, 0)

  def like(user: User, movie: Movie) = dao.setComponentWeight(user, movie, defaultFollowerWeight)

  def rate(user: User, movie: Movie, rating: Double) = dao.setComponentWeight(user, movie, rating * defaultRateMultiplicator)

  /*
        Private
   */

  private def md5(sample: String): String = {
    val digest = MessageDigest.getInstance("MD5")
    digest.reset()
    digest.update(sample.getBytes)
    digest.digest().map(0xFF & _).map {"%02x".format(_)}.foldLeft(""){_ + _}
  }
}