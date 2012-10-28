package com.owlunit.web.model

import common.{IiTagRecord, IiStringField, IiMongoRecord}
import org.bson.types.ObjectId
import org.joda.time.DateTime

import net.liftweb.common._
import net.liftweb.http.SessionVar
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.util.FieldContainer
import net.liftweb.util.Helpers._
import net.liftweb.common.Loggable
import net.liftmodules.mongoauth._
import net.liftmodules.mongoauth.model._

import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.lib.ui.IiTag
import net.liftweb.json.JsonAST.JValue

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class User private() extends ProtoAuthUser[User] with ObjectIdPk[User] with IiTagRecord[User] with IiTag {
  def meta = User

  // IiDao backend components
  ////////////////////////////////

  var ii: Ii = null
  override def tagCaption = this.name.is.toString
  override def tagType = "User"
  override def tagUrl = "#" //TODO(Anton) implement permalinks

  override def userIdAsString = id.toString()

  object loginContinueUrl extends StringField(this, 64, "/me")

  // Bio-like-info and Facebook
  ////////////////////////////////

  object facebookId extends IntField(this)
  def isConnectedToFaceBook = facebookId.is != 0

  object name extends StringField(this, "")

  object cover extends StringField(this, "http://placehold.it/606x60")
  object photo extends StringField(this, "http://placehold.it/150x150")

  object bio extends TextareaField(this, 0)
  object location extends StringField(this, 64)
  object locale extends StringField(this, 8, "")

  // Domain fields
  ////////////////////////////////

  object friends extends MongoListField[User, ObjectId](this)

  object movies extends MongoListField[User, ObjectId](this)
  object persons extends MongoListField[User, ObjectId](this)
  object keywords extends MongoListField[User, ObjectId](this)

  // Helpers and tech
  ////////////////////////////////

  def whenCreated: DateTime = new DateTime(id.is.getTime)

}

object User extends User with ProtoAuthUserMeta[User] with Loggable {
  import net.liftweb.mongodb.BsonDSL._

  // Mongo config
  ////////////////////////////////

  override def collectionName = "users"
  ensureIndex((informationItemId.name -> 1), unique = true)
  ensureIndex((email.name             -> 1), unique = true)
  ensureIndex((username.name          -> 1), unique = true)
  ensureIndex((facebookId.name        -> 1), unique = true)

  private lazy val indexUrl = MongoAuth.indexUrl.vend
  private lazy val registerUrl = MongoAuth.registerUrl.vend
  private lazy val loginTokenAfterUrl = MongoAuth.loginTokenAfterUrl.vend

  // IiDao dependency
  ////////////////////////////////

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  // CRUD and Find
  ////////////////////////////////

  override def createRecord: User = {
    val result = super.createRecord
    result.ii = iiDao.create
    result
  }

  def fromFacebookJson(json: JValue) = tryo {
    createRecord
      .facebookId((json \ "id").values.asInstanceOf[String].toInt)
      .name((json \ "name").values.asInstanceOf[String])
      .cover((json \ "cover" \ "source").values.asInstanceOf[String])
      .email((json \ "email").values.asInstanceOf[String])
  }

  def findByEmail(in: String): Box[User] = find(email.name, in)
  def findByFacebookId(in: Int): Box[User] = find(facebookId.name, in)
  def findByUsername(in: String): Box[User] = find(username.name, in)
  def findByStringId(id: String): Box[User] = if (ObjectId.isValid(id)) find(new ObjectId(id)) else Empty

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