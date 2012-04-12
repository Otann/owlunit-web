package com.owlunit.service.cinema.impl

import com.owlunit.core.ii.{Ii, IiDao}
import collection.mutable.ListBuffer
import com.owlunit.service.cinema.exception.CinemaException
import com.owlunit.service.cinema.{PersonService, PersonIi}

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object PersonServiceImpl {


  private[cinema] val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeyName       = MetaKeyPrefix + ".NAME"
  private[cinema] val KeySurname    = MetaKeyPrefix + ".SURNAME"
  private[cinema] val KeySimpleName = MetaKeyPrefix + ".SIMPLE_NAME"
  private[cinema] val KeySearch   = MetaKeyPrefix + ".FULL_NAME"

  private[cinema] def extract(dao: IiDao, items: Seq[Ii]): Seq[PersonIi] = {
    val extracted = items.map(item => extractOne(dao, item))
    for (Some(person) <- extracted) yield person
  }

  private[cinema] def extractOne(dao: IiDao, item: Ii): Option[PersonIi] = {
    val meta = withMeta(dao, item)
    meta.metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some(new PersonIi(
        meta.id,
        meta.metaValue(PersonServiceImpl.KeyName).get,
        meta.metaValue(PersonServiceImpl.KeySurname).get
      ))
    }
  }

  private def simplifyName(name: String,  surname: String) = simplifyComplexName(name, "##", surname)
}

trait PersonServiceImpl extends PersonService {
  import PersonServiceImpl._

  def dao: IiDao

  def createPerson(sample: PersonIi): PersonIi = {
    val item = dao.createIi
    dao.setMetaUnindexed(item, MetaKeyPrefix, "#")

    val searchValue = "%s %s" format (sample.name.toLowerCase, sample.surname.toLowerCase)
    dao.setMeta(item, KeySearch, searchValue)
    dao.setMeta(item, CinemaServiceImpl.KeySearch, searchValue)
    dao.setMetaUnindexed(item, KeyName, sample.name)
    dao.setMetaUnindexed(item, KeySurname, sample.surname)
    dao.setMetaUnindexed(item, KeySimpleName, simplifyName(sample.name, sample.surname))

    sample.copy(id = item.id)
  }

  def loadPerson(id: Long): Option[PersonIi] = extractOne(dao, dao.load(id))

  def loadPerson(name: String, surname: String): Option[PersonIi] = {
    val items = dao.load(KeySimpleName, simplifyName(name, surname))
    items.size match {
      case 0 => None
      case 1 => extractOne(dao, items.iterator.next())
      case _ => throw new CinemaException("Ambiguous load")
    }
  }

  def loadOrCreatePerson(name: String, surname: String): PersonIi = loadPerson(name, surname) match {
    case Some(x) => x
    case None => createPerson(new PersonIi(0, name, surname))
  }

  def searchPerson(query: String): Seq[PersonIi] = {
    val items = dao.search(KeySearch, buildQuery(query))
    val iterator = items.iterator
    val result = ListBuffer[PersonIi]()
    while (iterator.hasNext) {
      val k = extractOne(dao, iterator.next())
      if (k.isDefined)
        result += k.get
    }

    result
  }

}



