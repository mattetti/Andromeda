package controllers

/**
 * Created with IntelliJ IDEA.
 * User: mattetti
 * Date: 3/11/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */

import play.api.mvc.{Action, Controller}
import models.User
import play.api.libs.concurrent.Execution.Implicits._

object Users extends Controller {

  def show(login: String) = Action { implicit request =>
    val user =  User.findByLogin(login)
    Async {
     user.map(_ match {
      case Some(userObj) => Ok(userObj.asJson)
      case None          => NotFound
     })
    }
  }

}
