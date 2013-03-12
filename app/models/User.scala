package models
import java.util.Date;
import play.api.libs.ws._
import play.api.libs.json.Json._
import play.api.libs.json.JsValue
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import concurrent.Future

/**
 * Created with IntelliJ IDEA.
 * User: mattetti
 * Date: 3/11/13
 * Time: 6:19 PM
 * To change this template use File | Settings | File Templates.
 */
case class User( id: Int, login: String, name: String, created_at: String ){

  implicit val userWrites = new Writes[User] {
    def writes(user: User): JsValue = Json.obj(
      "id" -> user.id,
      "login" -> user.login,
      "name" -> user.name,
      "created_at" -> user.created_at
    )
  }

  def asJson = toJson(User.this)
}

object User {

  val baseUrl = "https://api.github.com/users/"

  def findByLogin(login: String): Future[User] = {
    WS.url(baseUrl + login).get().map { response =>
      User.fromJson(response.json)
    }
  }

   def fromJson(body: JsValue): User = {
     User(
      (body \ "id").as[Int],
      (body \ "login").as[String],
      (body \ "name").as[String],
      (body \ "created_at").as[String]
     )
  }
}