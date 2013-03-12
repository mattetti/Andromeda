/**
 * Created with IntelliJ IDEA.
 * User: mattetti
 * Date: 3/11/13
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */

package models
import java.util.Date;
import java.util.concurrent.TimeUnit
import play.api.libs.ws._
import play.api.libs.json.Json._
import play.api.libs.json.JsValue
import play.api.libs.json._
import play.api.libs.concurrent._
import scala.concurrent._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._
import concurrent.Future
import javassist.NotFoundException

case class GithubUser( id: Int, login: String, name: String, created_at: String, sshKeys: Seq[String] = List()){

  implicit val userWrites = new Writes[GithubUser] {
    def writes(user: GithubUser): JsValue = Json.obj(
      "id" -> user.id,
      "login" -> user.login,
      "name" -> user.name,
      "created_at" -> user.created_at,
      "keys" -> user.sshKeys
    )
  }

  def asJson = toJson(GithubUser.this)
}

object GithubUser {

  val baseUrl = "https://api.github.com/users/"

  def userDetails(login: String): Future[Option[JsValue]] = {
    WS.url(baseUrl + login).get().orTimeout("Timeout", 3, TimeUnit.SECONDS ).map { response =>
      response match {
        // Left and Right and used to define what Type is returned by the Timeout
        case Left(resp) => {
          if (resp.status == 200){
            Option(resp.json)
          } else {
            val debuggedResponse = resp.body.toString()
            play.Logger.error(s"Fetching GitHub's profile for $login failed: $debuggedResponse")
            None
          }
        }
        case _ => {
          play.Logger.error(s"Fetching GitHub's profile for $login timed out.")
          None
        }
      }

    }
  }

  def findByLogin(login: String): Future[Option[GithubUser]] = {
    for {
      details <- userDetails(login)
      info <- future { fromDetails(details) }
      keys <- sshKeysForLogin(login)
    } yield { info match {
        case Some(info) => Some(GithubUser(info.id, info.login, info.name, info.created_at, keys))
        case _ => None
        }
      }
  }

  def sshKeysForLogin(login: String): Future[Seq[String]] = {
    val url = s"https://api.github.com/users/$login/keys"
    WS.url(url).get().orTimeout("Timeout", 3, TimeUnit.SECONDS ).map { response =>
      response match {
        case Left(resp) => {
          if (resp.status == 200){
            (resp.json \\ "key").map( _.as[String] )
          } else {
            List()
          }
        }
        case _ => List()
      }
    }
  }

  def fromDetails(details: Option[JsValue]) = details match {
    case Some(body) =>
     Some(GithubUser((body \ "id").as[Int],
         (body \ "login").as[String],
         (body \ "name").as[String],
         (body \ "created_at").as[String]
     ))
    case _ => None
  }

  def fromJson(bodyOpt: Option[JsValue]): Option[GithubUser] = bodyOpt match {
    case Some(body) =>
      Some(GithubUser(
        (body \ "id").as[Int],
        (body \ "login").as[String],
        (body \ "name").as[String],
        (body \ "created_at").as[String]
      ))
    case _ => None
  }
}