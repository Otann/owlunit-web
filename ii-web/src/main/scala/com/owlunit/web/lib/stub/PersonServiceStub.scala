package com.owlunit.web.lib.stub

import com.owlunit.service.cinema.{PersonIi, PersonService}


/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */
object PersonServiceStub extends PersonServiceStub {

  val pacino = new PersonIi(11, "Al", "Pacino")
  val ford = new PersonIi(12, "Harrison", "Ford")
  val lukas = new PersonIi(12, "Jorge", "Lukas")

}

trait PersonServiceStub extends PersonService {
  import PersonServiceStub._

  def createPerson(sample: PersonIi) = ford

  def loadPerson(id: Long) = Some(ford)

  def loadPerson(name: String, surname: String) = Some(ford)

  def loadOrCreatePerson(name: String, surname: String) = ford

  def searchPerson(prefix: String) = List(pacino, ford)
}