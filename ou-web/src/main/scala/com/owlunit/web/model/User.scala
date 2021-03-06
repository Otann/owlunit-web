package com.owlunit.web.model

import common.{IiTagMetaRecord, IiTagRecord}
import org.bson.types.ObjectId
import org.joda.time.DateTime

import net.liftweb.common._
import net.liftweb.http.SessionVar
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.util.Helpers._
import net.liftweb.common.Loggable
import net.liftmodules.mongoauth._
import net.liftmodules.mongoauth.model._

import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.lib.ui.IiTag
import net.liftweb.json.JsonAST.JValue
import com.mongodb.DBObject

/**
 * @author Anton Chebotaev
 *         Copyright OwlUnit
 */

class User private() extends ProtoAuthUser[User] with ObjectIdPk[User] with IiTagRecord[User] with IiTag with Loggable {
  def meta = User

  // for IiTagRecord
  ////////////////////////////////

  var ii: Ii = null

  override def name = "%s %s" format (this.firstName.is, this.lastName.is)
  override def kind = "user"

  // Bio-like-info and Facebook
  ////////////////////////////////

  object facebookId extends LongField(this)
  def isConnectedToFaceBook = facebookId.is != 0

  object firstName extends StringField(this, "")
  object lastName extends StringField(this, "")

  object cover extends StringField(this, "http://fakeimg.pl/606x60")
  object photo extends StringField(this, "http://fakeimg.pl/150x150")

  object bio extends TextareaField(this, 0)
  object location extends StringField(this, 64)
  object locale extends StringField(this, 8, "")

  object loginContinueUrl extends StringField(this, 64, "/me")


  // Domain fields and modifiers
  ////////////////////////////////

  def movies = Movie.loadFromIis(ii.items.keys)
  def persons = Person.loadFromIis(ii.items.keys)
  def keywords = Keyword.loadFromIis(ii.items.keys)
  def respects = User.loadFromIis(ii.items.keys)

  object friends extends MongoListField[User, ObjectId](this)

  def addTag(tag: IiTagRecord[_]) = {
    val weight = tag match {
      case k: Keyword => 7.0
      case p: Person => 10.0
      case m: Movie => 5.0
      case _ => 0.0
    }
    this.ii.setItem(tag.ii, weight)
    this
  }

  def hasItem(iiId: String) = ii.items.keys.toSet.contains((item: Ii) => item.meta(metaGlobalObjectId) == iiId)

  // Helpers and tech
  ////////////////////////////////

  def whenCreated: DateTime = new DateTime(id.is.getTime)

  override def userIdAsString = id.toString()

}

object User extends User with ProtoAuthUserMeta[User] with IiTagMetaRecord[User] {
  import net.liftweb.mongodb.BsonDSL._

  // Mongo config
  ////////////////////////////////

  override def collectionName = "users"
  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((email.name             -> 1), unique = true)
  ensureIndex((facebookId.name        -> 1), unique = true)

//  private lazy val indexUrl = MongoAuth.indexUrl.vend
//  private lazy val registerUrl = MongoAuth.registerUrl.vend
//  private lazy val loginTokenAfterUrl = MongoAuth.loginTokenAfterUrl.vend

  // IiDao dependency
  ////////////////////////////////

  // Creation and fetching
  ////////////////////////////////

  override def fromDBObject(dbo: DBObject) = {
    val result = super.fromDBObject(dbo)
    result.ii = iiDao.load(result.informationItemId.is)
    result
  }


  def fromFacebookJson(json: JValue) = tryo {
    createRecord
      .facebookId((json \ "id").values.asInstanceOf[String].toInt)
      .firstName((json \ "first_name").values.asInstanceOf[String])
      .lastName((json \ "last_name").values.asInstanceOf[String])
      .cover((json \ "cover" \ "source").values.asInstanceOf[String])
      .email((json \ "email").values.asInstanceOf[String])
  }

  override def findByStringId(id: String): Box[User] = this.find(id)
  def findFromFacebook(facebookIdIn: Int, emailIn: String): Box[User] =
    find(facebookId.name, facebookIdIn) or find(email.name, emailIn)

  //TODO(Anton): refactor
  def findByEmail(in: String): Box[User] = find(email.name, in)
  def findByUsername(in: String): Box[User] = find(username.name, in)

  // Tech stuff
  ////////////////////////////////

  override def onLogIn: List[User => Unit] = List((user: User) => User.loginCredentials.remove())
  override def onLogOut: List[Box[User] => Unit] = List((x: Box[User]) => x.foreach { u => ExtSession.deleteExtCookie() })


  // External Session
  ////////////////////////////////

  def createExtSession(uid: ObjectId) { ExtSession.createExtSession(uid) }

  // used during login process
  object loginCredentials extends SessionVar[LoginCredentials](LoginCredentials("", 0))

  // asInstanceOf[User] is used to dismiss IDEA error report
  object sessionUser extends SessionVar[User](createRecord.email(loginCredentials.is.email).asInstanceOf[User])

}

case class LoginCredentials(email: String, facebookId: Int)