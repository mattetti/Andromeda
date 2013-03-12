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
import play.api.libs.concurrent.Execution.Implicits._
import concurrent.Future
import javassist.NotFoundException

case class GithubUser( id: Int, login: String, name: String, created_at: String ){

  implicit val userWrites = new Writes[GithubUser] {
    def writes(user: GithubUser): JsValue = Json.obj(
      "id" -> user.id,
      "login" -> user.login,
      "name" -> user.name,
      "created_at" -> user.created_at
    )
  }

  def asJson = toJson(GithubUser.this)
}

object GithubUser {

  val baseUrl = "https://api.github.com/users/"

  def findByLogin(login: String): Future[Option[GithubUser]] = {
    WS.url(baseUrl + login).get().orTimeout("Timeout", 3, TimeUnit.SECONDS ).map { response =>
      response match {
        // Left and Right and used to define what Type is returned by the Timeout
        case Left(resp) => {
          if (resp.status == 200){
            Option(GithubUser.fromJson(resp.json))
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

   def fromJson(body: JsValue): GithubUser = {
     GithubUser(
      (body \ "id").as[Int],
      (body \ "login").as[String],
      (body \ "name").as[String],
      (body \ "created_at").as[String]
     )
  }
}