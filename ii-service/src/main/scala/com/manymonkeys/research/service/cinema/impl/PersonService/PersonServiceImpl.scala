package com.manymonkeys.research.service.cinema.impl.PersonService

import com.manymonkeys.service.cinema.PersonService
import com.manymonkeys.model.cinema.{Role, Person}
import com.manymonkeys.research.service.utils.IiDaoAccess
import com.manymonkeys.core.ii.Ii
import scalaj.collection.Imports._
import scala.collection.JavaConversions._
import PersonServiceImpl._

/**
* @author Anton Chebotaev
*         Owls Proprietary
*/
object PersonServiceImpl {

  final val KEY_NAME       = classOf[PersonService].getName + ".NAME"
  final val KEY_SURNAME    = classOf[PersonService].getName + ".SURNAME"
  final val KEY_FULL_NAME  = classOf[PersonService].getName + ".FULL_NAME"
  final val KEY_ROLES      = classOf[PersonService].getName + ".ROLES"
  final val ROLES_DELIMITER     = "#"
  final val FULLNAME_DELIMITER  = " "

}

class PersonServiceImpl extends PersonService
                        with IiDaoAccess
                        with PersonConverter {

  implicit def iiToPerson(item: Ii):Person = {
    val meta = itemWithMeta(item)
    Person(
      name    = meta.getMeta(KEY_NAME),
      surname = meta.getMeta(KEY_SURNAME),
      roles   = unpackRoles(meta.getMeta(KEY_ROLES)).asJava,
      uuid    = meta.getUUID
    )
  }

  def createPerson(person: Person): Person = {
    val item = dao.createInformationItem()
    dao.setMeta(item, KEY_NAME, person.name)
    dao.setMeta(item, KEY_SURNAME, person.surname)
    dao.setUnindexedMeta(item, KEY_FULL_NAME, fullName(person))
    dao.setUnindexedMeta(item, KEY_ROLES, packRoles(person.roles))
  }


  def addRole(person: Person, role: Role) = {
    val item = personToIi(person)
    val roles = unpackRoles(itemWithMeta(item).getMeta(KEY_ROLES))
    dao.setMeta(item, KEY_ROLES, packRoles(roles + role))
  }

  def findOrCreate(person: Person) = iiToPerson(personToIi(person))

  def findOrCreate(person: Person, role: Role): Person = {
    val truePerson = findOrCreate(person)
    addRole(truePerson, role)
  }

  private def packRoles(roles: java.util.Set[Role]) = roles.map(_.name).reduceLeft(_ + ROLES_DELIMITER + _)
  private def unpackRoles(raw: String) = raw.split(ROLES_DELIMITER).map(Role.valueOf(_)).toSet

}