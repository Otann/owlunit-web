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

  def loadRandomUser = {
    val id = createRandomUser.save.id.is
    User.find(id).open_!
  }

  def createRandomMovie = {
    val movie = Movie.createRecord
    movie.name(randomString)
    movie.year(2000)
    movie
  }

  def loadRandomMovie = {
    val id = createRandomMovie.save.id.is
    Movie.find(id).open_!
  }

  def createRandomKeyword = {
    val keyword = Keyword.createRecord
    keyword.name(randomString)
    keyword
  }

  def loadRandomKeyword = {
    val id = createRandomKeyword.save.id.is
    Keyword.find(id).open_!
  }

  def createRandomPerson = {
    val person = Person.createRecord
    person.firstName(randomString)
    person.lastName(randomString)
    person
  }

  def loadRandomPerson = {
    val id = createRandomPerson.save.id.is
    Person.find(id).open_!
  }

}
