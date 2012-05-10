package com.owlunit.web.model

import org.bson.types.ObjectId
import org.joda.time.DateTime

import net.liftweb._
import common._
import http.{StringField => _, BooleanField => _, _}
import mongodb.record.field._
import record.field._
import util.FieldContainer

import net.liftmodules.mongoauth._
import net.liftmodules.mongoauth.field._
import net.liftmodules.mongoauth.model._
import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class User private () extends ProtoAuthUser[User] with ObjectIdPk[User] {
  def meta = User

  def userIdAsString: String = id.toString()

  val iiFootprint = this.getClass.getName + ".MongoId"
  val iiMetaName  = this.getClass.getName + ".Name"

  protected var ii: Box[Ii] = Empty
  object iiid extends LongField(this)

  object name extends StringField(this, 64) {
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

  /*
   * FieldContainers for various LiftScreeens.
   */
  def accountScreenFields = new FieldContainer {
    def allFields = List(username, email)
  }

  def profileScreenFields = new FieldContainer {
    def allFields = List(name, location, bio)
  }

  def registerScreenFields = new FieldContainer {
    def allFields = List(username, email, password)
  }

  def whenCreated: DateTime = new DateTime(id.is.getTime)

  override def save = {
    ii.map(_.save)
    super.save
  }

}

object User extends User with ProtoAuthUserMeta[User] with Loggable {
  import mongodb.BsonDSL._

  override def collectionName = "users"

  ensureIndex((email.name -> 1), true)
  ensureIndex((username.name -> 1), true)

  lazy val iiDao = DependencyFactory.iiDao.vend //TODO unsafe vend

  override def createRecord = {
    val result = super.createRecord
    val ii = iiDao.create.setMeta(iiFootprint, result.id.toString())
    result.iiid(ii.id)
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

  // asInstanceOf[User] is usd to dismiss IDEA error report
  object sessionUser extends SessionVar[User](createRecord.email(loginCredentials.is.email).asInstanceOf[User])
}

case class LoginCredentials(email: String)