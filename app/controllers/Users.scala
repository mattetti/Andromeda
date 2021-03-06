package controllers

/**
 * Created with IntelliJ IDEA.
 * GithubUser: mattetti
 * Date: 3/11/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */

import play.api.mvc.{Action, Controller}
import models.GithubUser
import play.api.libs.concurrent.Execution.Implicits._

object Users extends Controller {

  def show(login: String) = Action { implicit request =>

      Async {
        // this makes 2 async calls
        GithubUser.findByLogin(login).map {
          _.map( user => Ok(user.asJson) )
           .getOrElse(NotFound)
        }
      }

  }

}
