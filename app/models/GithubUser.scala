/**
 * Created with IntelliJ IDEA.
 * User: mattetti
 * Date: 3/11/13
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */

package models
import java.util.concurrent.TimeUnit
import play.api.libs.ws._
import play.api.libs.json.Json._
import play.api.libs.json._
import play.api.libs.concurrent._
import scala.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import concurrent.Future
import models.ApiFetcher

case class GithubUser( id: Int, login: String, name: String,
                       created_at: String,
                       sshKeys: Seq[String] = List() ) {

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

object GithubUser extends ApiFetcher {

  val baseUrl = "https://api.github.com/users/"

  def userDetails(login: String): Future[Option[JsValue]] = {
    get(baseUrl + login)
  }

  /*
     To create a github user, we fetch data from 2 different endpoints.
     And wrap the result in an Option in case something wrong happens in the futures.
   */
  def findByLogin(login: String): Future[Option[GithubUser]] = {
    val userInfo = userDetails(login).map(fromDetails)
    val sshKeys = sshKeysForLogin(login)

    for {
      info <- userInfo
      keys <- sshKeys
    } yield { info match {
        case Some(info) => Some(GithubUser(info.id, info.login, info.name, info.created_at, keys))
        case _ => None
       }
    }
  }

  def sshKeysForLogin(login: String): Future[Seq[String]] = {
    get( s"https://api.github.com/users/$login/keys" ).map {
      _.map(jsonBody => (jsonBody \\ "key").map( _.as[String] ) )
       .getOrElse(List())
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