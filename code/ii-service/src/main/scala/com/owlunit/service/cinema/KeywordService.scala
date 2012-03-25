package com.owlunit.service.cinema

import com.owlunit.core.ii.{Ii, IiDao}
import collection.mutable.ListBuffer

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class Keyword(override val id: Long, val name: String) extends CinemaItem(id) {

    def copy(id: Long) = new Keyword(id, this.name)

}

object KeywordService {

  private[cinema] val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeyName = MetaKeyPrefix + ".NAME"

  private[cinema] def extract(dao: IiDao, items: Seq[Ii]): Seq[Keyword] = {
    val extracted = items.map(item => extractOne(dao, item))
    for (Some(keyword) <- extracted) yield keyword
  }

  private[cinema] def extractOne(dao: IiDao, item: Ii): Option[Keyword] = {
    withMeta(dao, item).metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some(new Keyword(
        item.id,
        item.metaValue(KeywordService.KeyName).get
      ))
    }
  }

}

class KeywordService(dao: IiDao) {
  import KeywordService._

  def create(sample: Keyword): Keyword = {
    val item = dao.createIi
    dao.setMetaUnindexed(item, MetaKeyPrefix, "#")
    dao.setMeta(item, KeyName, sample.name)
    sample.copy(id = item.id)
  }

  def load(name: String): Option[Keyword] = {
    val items = dao.load(KeyName, name)
    items.size match {
      case 0 => None
      case 1 => extractOne(dao, items.iterator.next())
      case _ => throw new CinemaException("Ambiguous load")
    }
  }

  def loadOrCreate(name: String): Keyword = load(name) match {
    case Some(x) => x
    case None => create(new Keyword(0, name))
  }

  def search(prefix: String): Seq[Keyword] = {
    val items = dao.search(KeyName, "%s*" format  prefix)
    val iterator = items.iterator
    val result = ListBuffer[Keyword]()
    while (iterator.hasNext) {
      val k = extractOne(dao, iterator.next())
      if (k.isDefined)
        result += k.get
    }

    result
  }
}
