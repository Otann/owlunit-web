package com.owlunit.service.cinema

import impl.PersonServiceImpl
import com.owlunit.core.ii.IiDao

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Person (override val id: Long,
              val name: String,
              val surname: String) extends CinemaItem(id) {

  def copy(id: Long) = new Person(id, this.name, this.surname)

}

trait PersonService {

  def create(sample: Person): Person
  def load(name: String, surname: String): Option[Person]
  def loadOrCreate(name: String, surname: String): Person
  def search(prefix: String): Seq[Person]

}

object PersonService {

  def apply(dao: IiDao): PersonService = new PersonServiceImpl(dao)

}
