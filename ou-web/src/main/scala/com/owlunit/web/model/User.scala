package com.owlunit.web.model

import common.{IiStringField, IiMongoRecord}
import org.bson.types.ObjectId
import org.joda.time.DateTime

import net.liftweb.common._
import net.liftweb.http.{BooleanField => _, StringField => _, _}
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.util.FieldContainer

import net.liftmodules.mongoauth._
import net.liftmodules.mongoauth.field._
import net.liftmodules.mongoauth.model._
import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.lib.{IiTag, IiMeta}
import net.liftweb.common.Loggable

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class User private() extends ProtoAuthUser[User] with ObjectIdPk[User] with IiMongoRecord[User]  with IiMeta with IiTag {
  def meta = User
  def baseMeta = "ii.user"

  var ii: Ii = null

  def tagId = this.id.is.toString
  def tagCaption = this.name.is.toString
  def tagUrl = "#" //TODO(Anton) implement permalinks

  def userIdAsString: String = id.toString()

  object name extends IiStringField(this, ii, Name, "") {
    override def displayName = "Name"
    override def validations =
      valMaxLen(64, "Name must be 64 characters or less") _ ::
      super.validations
  }
  object location extends StringField(this, 64) {
    override def displayName = "Location"
    override def validations =
      valMaxLen(64, "Location must be 64 characters or less") _ ::
      super.validations
  }
  object bio extends TextareaField(this, 160) {
    override def displayName = "Bio"
    override def validations =
      valMaxLen(160, "Bio must be 160 characters or less") _ ::
      super.validations
  }

  object backdrop extends StringField(this, "http://placehold.it/606x60")

  object movies extends MongoListField[User, ObjectId](this)
  object friends extends MongoListField[User, ObjectId](this)
  object keywords extends MongoListField[User, ObjectId](this)

  /*
   * FieldContainers for various Lift Screens.
   */
  def accountScreenFields = new FieldContainer {
    def allFields = List(username, email)
  }

  def profileScreenFields = new FieldContainer {
    def allFields = List(name, location, bio)
  }

  def registerScreenFields = new FieldContainer {
    def allFields = List(name, username, email, password)
  }

  def whenCreated: DateTime = new DateTime(id.is.getTime)

}

object User extends User with ProtoAuthUserMeta[User] with Loggable {
  import net.liftweb.mongodb.BsonDSL._

  override def collectionName = "users"

  ensureIndex((informationItemId.name -> 1), true)
  ensureIndex((email.name -> 1), true)
  ensureIndex((username.name -> 1), true)

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    result.ii = iiDao.create.setMeta(Footprint, result.id.toString())
    result
  }

  def findByEmail(in: String): Box[User] = find(email.name, in)
  def findByUsername(in: String): Box[User] = find(username.name, in)
  def findByStringId(id: String): Box[User] = if (ObjectId.isValid(id)) find(new ObjectId(id)) else Empty

  override def onLogIn: List[User => Unit] = List((user: User) => User.loginCredentials.remove())
  override def onLogOut: List[Box[User] => Unit] = List(
    (x: Box[User]) => logger.debug("User.onLogOut called."),
    (x: Box[User]) => x.foreach { u => ExtSession.deleteExtCookie() }
  )

  /*
   * MongoAuth vars
   */
  private lazy val indexUrl = MongoAuth.indexUrl.vend
  private lazy val registerUrl = MongoAuth.registerUrl.vend
  private lazy val loginTokenAfterUrl = MongoAuth.loginTokenAfterUrl.vend

  /*
   * ExtSession
   */
  def createExtSession(uid: ObjectId) { ExtSession.createExtSession(uid) }

  // used during login process
  object loginCredentials extends SessionVar[LoginCredentials](LoginCredentials(""))

  // asInstanceOf[User] is used to dismiss IDEA error report
  object sessionUser extends SessionVar[User](createRecord.email(loginCredentials.is.email).asInstanceOf[User])
}

case class LoginCredentials(email: String)