package com.manymonkeys.service.cinema.impl.PersonService

import com.manymonkeys.model.cinema.Person
import com.manymonkeys.core.ii.Ii
import com.manymonkeys.service.utils.IiDaoAccess
import PersonServiceImpl._

/**
* @author Anton Chebotaev
*         Owls Proprietary
*/


trait PersonConverter extends IiDaoAccess {

  class PersonNotFoundException(person: Person) extends Exception("Referring person %s %s was not found in service" format (person.name, person.surname)){ }

  implicit def personToIi(person: Person): Ii = {
    if (person.uuid != null) {
      val item = dao.load(person.uuid)
      if (item == null) {
        throw new PersonNotFoundException(person)
      } else {
        item
      }
    } else {
      val items = dao.load(KEY_FULL_NAME, fullName(person))
      if (items.isEmpty) {
        throw new PersonNotFoundException(person)
      } else {
        items.iterator().next()
      }
    }
  }

  protected def fullName(person: Person) = person.name + FULLNAME_DELIMITER + person.surname

}