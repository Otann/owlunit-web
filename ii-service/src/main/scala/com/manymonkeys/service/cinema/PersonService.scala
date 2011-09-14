package com.manymonkeys.service.cinema

import com.manymonkeys.model.cinema.Person
import com.manymonkeys.model.cinema.Role

/**
* @author Ilya Pimenov
*         Owls Proprietary
*/
trait PersonService {

  def createPerson(person: Person): Person

  def addRole(person: Person, role: Role): Person

  def findOrCreate(person: Person): Person

  def findOrCreate(person: Person, role: Role): Person

}

