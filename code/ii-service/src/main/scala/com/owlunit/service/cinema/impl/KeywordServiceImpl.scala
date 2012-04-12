package com.owlunit.service.cinema.impl

import com.owlunit.core.ii.{Ii, IiDao}
import collection.mutable.ListBuffer
import com.owlunit.service.cinema.{KeywordService, KeywordIi}
import com.owlunit.service.cinema.exception.CinemaException

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object KeywordServiceImpl {

  private[cinema] val MetaKeyPrefix = this.getClass.getName
  private[cinema] val KeySearch = MetaKeyPrefix + ".SEARCH"
  private[cinema] val KeyName = MetaKeyPrefix + ".NAME"

  private[cinema] def extract(dao: IiDao, items: Seq[Ii]): Seq[KeywordIi] = {
    val extracted = items.map(item => extractOne(dao, item))
    for (Some(keyword) <- extracted) yield keyword
  }

  private[cinema] def extractOne(dao: IiDao, item: Ii): Option[KeywordIi] = {
    val meta = withMeta(dao, item)
    meta.metaValue(MetaKeyPrefix) match {
      case None => None
      case Some(_) => Some(new KeywordIi(
        meta.id,
        meta.metaValue(KeywordServiceImpl.KeyName).get
      ))
    }
  }

}

trait KeywordServiceImpl extends KeywordService {
  import KeywordServiceImpl._

  def dao: IiDao

  def createKeyword(name: String) = createKeyword(new KeywordIi(0, name))

  def createKeyword(sample: KeywordIi): KeywordIi = {
    val item = dao.createIi
    dao.setMetaUnindexed(item, MetaKeyPrefix, "#")
    dao.setMeta(item, KeyName, sample.name)
    dao.setMeta(item, KeySearch, sample.name.toLowerCase)
    dao.setMeta(item, CinemaServiceImpl.KeySearch, sample.name.toLowerCase)
    sample.copy(id = item.id)
  }


  def loadKeyword(id: Long): Option[KeywordIi] = extractOne(dao, dao.load(id))

  def loadKeyword(name: String): Option[KeywordIi] = {
    val items = dao.load(KeyName, name)
    items.size match {
      case 0 => None
      case 1 => extractOne(dao, items.iterator.next())
      case _ => throw new CinemaException("Ambiguous load")
    }
  }

  def loadOrCreateKeyword(name: String): KeywordIi = loadKeyword(name) match {
    case Some(x) => x
    case None => createKeyword(new KeywordIi(0, name))
  }

  def searchKeyword(query: String): Seq[KeywordIi] = {
    val items = dao.search(KeySearch, buildQuery(query))
    val iterator = items.iterator
    val result = ListBuffer[KeywordIi]()
    while (iterator.hasNext) {
      val k = extractOne(dao, iterator.next())
      if (k.isDefined)
        result += k.get
    }

    result
  }
}






