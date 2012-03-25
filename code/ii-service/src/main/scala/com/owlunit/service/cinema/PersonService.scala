package com.owlunit.service.cinema

import com.owlunit.core.ii.{Ii, IiDao}
import collection.mutable.ListBuffer

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Person (override val id: Long,
              val name: String,
              val surname: String) extends CinemaItem(id) {

  def copy(id: Long) = new Person(id, this.name, this.surname)

}

object PersonService {


  private[cinema] val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeyName       = MetaKeyPrefix + ".NAME"
  private[cinema] val KeySurname    = MetaKeyPrefix + ".SURNAME"
  private[cinema] val KeySimpleName = MetaKeyPrefix + ".SIMPLE_NAME"
  private[cinema] val KeyFullName   = MetaKeyPrefix + ".FULL_NAME"

  private[cinema] def extract(dao: IiDao, items: Seq[Ii]): Seq[Person] = {
    val extracted = items.map(item => extractOne(dao, item))
    for (Some(person) <- extracted) yield person
  }

  private[cinema] def extractOne(dao: IiDao, item: Ii): Option[Person] = {
    withMeta(dao, item).metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some(new Person(
        item.id,
        item.metaValue(PersonService.KeyName).get,
        item.metaValue(PersonService.KeySurname).get
      ))
    }
  }

  private def simplifyName(name: String,  surname: String) = simplifyComplexName(name, "##", surname)
}

class PersonService(dao: IiDao) {
  import PersonService._

  def create(sample: Person): Person = {
    val item = dao.createIi
    dao.setMetaUnindexed(item, MetaKeyPrefix, "#")

    dao.setMeta(item, KeyFullName, "%s %s" format (sample.name, sample.surname))
    dao.setMetaUnindexed(item, KeyName, sample.name)
    dao.setMetaUnindexed(item, KeySurname, sample.surname)
    dao.setMetaUnindexed(item, KeySimpleName, simplifyName(sample.name, sample.surname))

    sample.copy(id = item.id)
  }

  def load(name: String, surname: String): Option[Person] = {
    val items = dao.load(KeySimpleName, simplifyName(name, surname))
    items.size match {
      case 0 => None
      case 1 => extractOne(dao, items.iterator.next())
      case _ => throw new CinemaException("Ambiguous load")
    }
  }

  def loadOrCreate(name: String, surname: String): Person = load(name, surname) match {
    case Some(x) => x
    case None => create(new Person(0, name, surname))
  }

  def search(prefix: String): Seq[Person] = {
    val items = dao.search(KeyFullName, "%s*" format prefix)
    val iterator = items.iterator
    val result = ListBuffer[Person]()
    while (iterator.hasNext) {
      val k = extractOne(dao, iterator.next())
      if (k.isDefined)
        result += k.get
    }

    result
  }

}
