package com.owlunit.service.cinema

import impl.PersonServiceImpl
import com.owlunit.core.ii.IiDao

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class PersonIi (override val id: Long,
              val name: String,
              val surname: String) extends CinemaIi(id, name + " " + surname) {

  def copy(id: Long) = new PersonIi(id, this.name, this.surname)

  override def toString = "PersonIi(%s %s)" format (name, surname)
}

trait PersonService {

  def createPerson(sample: PersonIi): PersonIi
  def loadPerson(id: Long): Option[PersonIi]
  def loadPerson(name: String, surname: String): Option[PersonIi]
  def loadOrCreatePerson(name: String, surname: String): PersonIi
  def searchPerson(prefix: String): Seq[PersonIi]

}