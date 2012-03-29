package com.owlunit.service.cinema.impl

import com.owlunit.core.ii.{Ii, IiDao}
import collection.mutable.ListBuffer
import com.owlunit.service.cinema.{CinemaException, Person}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object PersonServiceImpl {


  private[cinema] val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeyName       = MetaKeyPrefix + ".NAME"
  private[cinema] val KeySurname    = MetaKeyPrefix + ".SURNAME"
  private[cinema] val KeySimpleName = MetaKeyPrefix + ".SIMPLE_NAME"
  private[cinema] val KeyFullName   = MetaKeyPrefix + ".FULL_NAME"

  def apply(dao: IiDao) = new PersonServiceImpl(dao)

  private[cinema] def extract(dao: IiDao, items: Seq[Ii]): Seq[Person] = {
    val extracted = items.map(item => extractOne(dao, item))
    for (Some(person) <- extracted) yield person
  }

  private[cinema] def extractOne(dao: IiDao, item: Ii): Option[Person] = {
    val meta = withMeta(dao, item)
    meta.metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some(new Person(
        meta.id,
        meta.metaValue(PersonServiceImpl.KeyName).get,
        meta.metaValue(PersonServiceImpl.KeySurname).get
      ))
    }
  }

  private def simplifyName(name: String,  surname: String) = simplifyComplexName(name, "##", surname)
}

class PersonServiceImpl(dao: IiDao) {
  import PersonServiceImpl._

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



