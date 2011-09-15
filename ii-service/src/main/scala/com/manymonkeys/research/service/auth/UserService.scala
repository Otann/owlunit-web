package com.manymonkeys.research.service.auth

import com.manymonkeys.model.auth.User
import com.manymonkeys.model.cinema.Movie

/**
 * @author Ilya Pimenov
 *         Owl Proprietary
 */
trait UserService {

  def createUser(user: User): User

  def getUser(login: String): User

  def setPassword(user: User, password: String): User

  def checkPassword(user: User, password: String): Boolean

  def follow(follower: User,  followed: User): User

  def unfollow(follower: User,  followed: User): User

  def like(user: User, movie: Movie): User

  def rate(user: User,  movie: Movie, rating: Double): User

}

