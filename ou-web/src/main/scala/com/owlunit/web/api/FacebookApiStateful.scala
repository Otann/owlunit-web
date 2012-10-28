package com.owlunit.web.api

import com.owlunit.web.lib.{ AppHelpers, FacebookGraph }
import com.owlunit.web.model.User
import net.liftweb._
import common._
import http._
import http.rest.RestHelper
import json._
import util.Helpers._
import com.owlunit.web.config.Site

/**
 * @author Anton Chebotaev
 *         Owls Proprietary
 */

object FacebookApiStateful extends RestHelper with AppHelpers with Loggable {

  serve("api" / "facebook" prefix {

    // This is the url that Facebook calls back to when authorizing a user
    case "auth" :: Nil Get _ => {

      if (S.param("code").isDefined) {

        val loadedUser: Box[User] = (for {
          code        <- S.param("code")
          state       <- S.param("state") ?~ "State not provided"
          ok          <- boolToBox(state == FacebookGraph.csrf.is) ?~ ("The state does not match. You may be a victim of CSRF. %s != %s" format (state, FacebookGraph.csrf.is))
          accessToken <- FacebookGraph.accessToken(code)
          json        <- FacebookGraph.me(accessToken)

          facebookId  <- extractId(json)
          name        <- extractString(json, _ \ "name") ?~ "no name provided"
          email       <- extractString(json, _ \ "email") ?~ "no email provided"
          picture     <- extractString(json, _ \ "picture" \ "data" \ "url") or Full("")
          cover       <- extractString(json, _ \ "cover" \ "source") or Full("")
          bio         <- extractString(json, _ \ "bio") or Full("")

        } yield {

          logger.debug("auth json: " + pretty(render(json)))

          // set the access token session var
          FacebookGraph.currentAccessToken(Full(accessToken))

          User.findFromFacebook(facebookId, email) match {

            // already connected
            case Full(user) => {
              // refresh photo and cover
              user.cover(cover).photo(picture).bio(bio).save
              logger.debug("Updated user with ii: %s" format user.ii)
              User.logUserIn(user, isAuthed = true, isRemember = true)
              user
            }

            // register new
            case _ => {
              val user = User.createRecord
              user.facebookId(facebookId)
              user.name(name)
              user.photo(picture)
              user.cover(cover)
              user.email(email)
              user.bio(bio)
              user.save

              logger.debug("Created user with ii: %s" format user.ii)
              // log in created user
              User.logUserIn(user, isAuthed = true, isRemember = true)
              user
            }
          }

        })

        loadedUser match {
          case Full(user)            => RedirectResponse(user.loginContinueUrl.is, S.responseCookies: _*)
          case Failure(reason, _, _) => handleError(reason)
          case _                     => handleError("Empty isNew decision")
        }

      } else (S.param("error"), S.param("error_reason"), S.param("error_description")) match {
        case (Full(error), Full(reason), Full(desc)) => handleError("User denied authorization")
        case _ => handleError("Unknown request type")

      }

    }

    /*
     * This is called by Facebook when a user deauthorizes this app on facebook.com
     */
    case "deauth" :: Nil Post _ => {

      (for {
        signedReq   <- S.param("signed_request")
        json        <- FacebookGraph.parseSignedRequest(signedReq)
        facebookId  <- extractUserId(json)
        email       <- extractString(json, _ \ "email")
        user        <- User.findFromFacebook(facebookId, email)
      } yield {
        // deauthorize facebook
        //TODO(Atnon) User.disconnectFacebook(user)
      }) match {
        case Full(_) =>
        case Failure(msg, _, _) => handleError(msg)
        case Empty => handleError("Unknown error")
      }

      OkResponse()
    }

    //    /*
    //     * Call this via ajax when checking login status with JavaScript SDK.
    //     * Sets the access token and current facebookId.
    //     */
    //    case "init" :: Nil Post _ => boxJsonToJsonResponse {
    //
    //      import JsonDSL._
    //      for {
    //        accessToken <- S.param("accessToken") ?~ "Token not provided"
    //        userId <- S.param("userID") ?~ "UserId not provided"
    //        facebookId <- asInt(userId) ?~ "Invalid Facebook user id"
    //        signedReq <- S.param("signedRequest") ?~ "Signed request not provided"
    //        expiresIn <- S.param("expiresIn") ?~ "ExpiresIn not provided"
    //        json <- FacebookGraph.parseSignedRequest(signedReq)
    //      } yield {
    //        val JString(code) = json \\ "code"
    //        logger.debug("expiresIn: " + expiresIn)
    //        // set the access token session var
    //        FacebookGraph.currentAccessToken(Full(AccessToken(accessToken, code)))
    //        // set the facebookId
    //        FacebookGraph.currentFacebookId(Full(facebookId))
    //        ("status" -> "ok")
    //      }
    //    }
    //
    //    /*
    //     * Log in a user by their facebookId
    //     */
    //    case "login" :: Nil Post _ => boxJsonToJsonResponse {
    //
    //      import JsonDSL._
    //      for {
    //        facebookId <- FacebookGraph.currentFacebookId.is ?~ "currentFacebookId not set"
    //        user <- User.findByFacebookId(facebookId) ?~ "User not found by facebookId"
    //      } yield {
    //        if (user.validate.length == 0) {
    //          User.logUserIn(user, true, true)
    //          ("url" -> User.loginContinueUrl.is)
    //        } else {
    //          User.regUser(user)
    //          ("url" -> Site.register.url)
    //        }
    //      }
    //    }

  })

  private def extractId(jv: JValue): Box[Int] = tryo {
    val JString(fbid) = jv \ "id"
    toInt(fbid)
  }

  private def extractUserId(jv: JValue): Box[Int] = tryo {
    val JString(fbid) = jv \ "user_id"
    toInt(fbid)
  }

  private def handleError(msg: String) = {
    logger.error(msg)
    S.error(msg)
    RedirectResponse(url(Site.error), S.responseCookies: _*)
  }

}