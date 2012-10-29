package com.owlunit.web.model

import net.liftweb.util.Helpers

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

trait ModelHelper {

  def randomLong = Helpers.nextNum
  def randomString = Helpers.nextFuncName

  def createRandomUser = {
    val user = User.createRecord
    user.facebookId(randomLong)
    user.name(randomString)
    user.email(randomString)
    user
  }

  def createRandomMovie = {
    val movie = Movie.createRecord
    movie.name(randomString)
    movie.year(2000)
    movie
  }

  def createRandomKeyword = {
    val keyword = Keyword.createRecord
    keyword.name(randomString)
    keyword
  }

  def createRandomPerson = {
    val person = Person.createRecord
    person.firstName(randomString)
    person.lastName(randomString)
    person
  }

}
