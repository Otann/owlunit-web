package com.owlunit.web.model

import net.liftweb.util.Helpers

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

trait ModelHelper {

  def randomLong = Helpers.nextNum
  def randomString = Helpers.nextFuncName

  def createRandomUser = {
    val user = User.createRecord
    user.facebookId(randomLong)
    user.firstName(randomString)
    user.lastName(randomString)
    user.email(randomString)
    user
  }

  def loadRandomUser = {
    val id = createRandomUser.save(safe = true).id.is
    User.find(id).open_!
  }

  def createRandomMovie = {
    val movie = Movie.createRecord
    movie.title(randomString).tmdbId(randomLong)

//    movie.release(2000)
    movie
  }

  def loadRandomMovie = {
    val id = createRandomMovie.save(safe = true).id.is
    Movie.find(id).open_!
  }

  def createRandomKeyword = {
    val keyword = Keyword.createRecord
    keyword.nameField(randomString).tmdbId(randomLong)
    keyword
  }

  def loadRandomKeyword = {
    val id = createRandomKeyword.save(safe = true).id.is
    Keyword.find(id).open_!
  }

  def createRandomPerson = {
    val person = Person.createRecord
    person.fullName(randomString).tmdbId(randomLong)
    person
  }

  def loadRandomPerson = {
    val id = createRandomPerson.save(safe = true).id.is
    Person.find(id).open_!
  }

}
