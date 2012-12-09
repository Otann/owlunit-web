package com.owlunit.web.model

import net.liftweb.record.field._
import net.liftweb.mongodb.record.field._
import net.liftweb.util.FieldContainer
import net.liftweb.common._

import com.owlunit.core.ii.mutable.Ii
import common.{IiTagMetaRecord, IiTagRecord}

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

class Movie private() extends IiTagRecord[Movie] with ObjectIdPk[Movie] with Loggable {

  // for MongoRecord
  def meta = Movie

  // for IiTagRecord and IiTag, init in meta object
  var ii: Ii = null

  override def kind = "movie"
  override def name = this.title.is

  // Fields

  object title extends StringField(this, "")
  object release extends DateField(this)

  object posterUrl   extends StringField(this, "/static/img/no_poster.png")
  object backdropUrl extends StringField(this, "/static/img/no_backdrop.png")

  object backdropUrls extends MongoListField[Movie, String](this)

  object trailer extends StringField(this, "")
  object tagline extends StringField(this, "")
  object overview extends StringField(this, "")

  object crew extends BsonRecordListField(this, CrewItem)
  object cast extends BsonRecordListField(this, CastItem)

  // Data manipulation

  // helper to define weight values
  private def weight(obj: Any) = obj match {
    case k: Keyword =>    2.0
//    case Job.Actor =>     5.0
//    case Job.Director => 10.0
//    case Job.Producer => 10.0
    case _ =>             1.0
  }

  // keywords are easy, just set weight

  def keywords = Keyword.loadFromIis(ii.items.keys)

  def addKeyword(keyword: Keyword) = this.ii.setItem(keyword.ii, weight(keyword))
  def removeKeyword(keyword: Keyword) = this.ii.removeItem(keyword.ii)

  def countUnique[T](map: collection.mutable.Map[T, Long], list: List[T]) {
    for (next <- list) {
      map.getOrElseUpdate(next, 0)
      map(next) += 1
    }
  }

  override def save = {
    val persons = collection.mutable.Map[Ii, Long]()
    countUnique(persons, crew.is.map(_.person.obj).flatten.map(_.ii))
    countUnique(persons, cast.is.map(_.person.obj).flatten.map(_.ii))

    for (existing <- ii.items.keys.filter(_.meta.getOrElse("king", "") == "person")) {
      if (!persons.contains(existing)) {
        ii.removeItem(existing)
      }
    }

    for ((current, value) <- persons) {
      ii.setItem(current, value)
    }

    super.save
  }

  // Field Containers

  def createFields = new FieldContainer { def allFields = List(title, release, posterUrl) }
  def editFields =   new FieldContainer { def allFields = List(title, release, posterUrl) }

}

object Movie extends Movie with IiTagMetaRecord[Movie] with Loggable {

  def findBySimpleName(name: String, year: Int): Box[Movie] = Empty

}
