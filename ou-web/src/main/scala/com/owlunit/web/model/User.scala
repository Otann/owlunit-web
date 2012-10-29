package com.owlunit.web.model

import common.{IiTagMetaContract, IiTagRecord}
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
import com.foursquare.rogue.Rogue._

import com.owlunit.core.ii.mutable.Ii
import com.owlunit.web.config.DependencyFactory
import com.owlunit.web.lib.ui.IiTag
import net.liftweb.json.JsonAST.JValue
import com.owlunit.core.ii.NotFoundException

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

class User private() extends ProtoAuthUser[User] with ObjectIdPk[User] with IiTagRecord[User] with IiTag with Loggable {
  def meta = User

  // IiDao backend components
  ////////////////////////////////

  var ii: Ii = null
  override def iiName = this.name.is.toString
  override def iiType = "user"

  override def userIdAsString = id.toString()

  object loginContinueUrl extends StringField(this, 64, "/me")

  // Bio-like-info and Facebook
  ////////////////////////////////

  object facebookId extends LongField(this)
  def isConnectedToFaceBook = facebookId.is != 0

  object name extends StringField(this, "")

  object cover extends StringField(this, "http://placehold.it/606x60")
  object photo extends StringField(this, "http://placehold.it/150x150")

  object bio extends TextareaField(this, 0)
  object location extends StringField(this, 64)
  object locale extends StringField(this, 8, "")

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
    logger.debug("adding ii: %s" format tag.ii)
    this.ii.setItem(tag.ii, weight)
    this
  }

  def hasItem(iiId: String) = ii.items.keys.toSet.contains(iiId)

  // Helpers and tech
  ////////////////////////////////

  def whenCreated: DateTime = new DateTime(id.is.getTime)

}

object User extends User with ProtoAuthUserMeta[User] with IiTagMetaContract[User] {
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

  private def loadIi(user: User): Box[User] = {
    try {
      user.ii = iiDao.load(user.informationItemId.is)
      Full(user)
    } catch {
      case e: NotFoundException => Failure("Unable to find linked ii", Full(e), Empty)
    }
  }

  override def find(oid: ObjectId) = super.find(oid).flatMap(loadIi)
  override def find(id: String): Box[User] = if (ObjectId.isValid(id)) find(new ObjectId(id)) else Empty

  override def findByStringId(id: String): Box[User] = this.find(id)
  def findFromFacebook(facebookIdIn: Int, emailIn: String): Box[User] =
    find(facebookId.name, facebookIdIn) or find(email.name, emailIn) flatMap (loadIi)

  //TODO(Anton): refactor
  def findByEmail(in: String): Box[User] = find(email.name, in).flatMap(loadIi)
  def findByUsername(in: String): Box[User] = find(username.name, in).flatMap(loadIi)

  protected[model] def loadFromIis(iis: Iterable[Ii]) = {
    val iiMap = iis.map(item => (item.id -> item)).toMap
    val query = User where (_.informationItemId in iiMap.keys)

    // Init ii before return
    query.fetch().map(user => {
      user.ii = iiMap(user.informationItemId.is)
      user
    })
  }

  def searchWithName(prefix: String) = loadFromIis(prefixSearch(prefix, iiDao))

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