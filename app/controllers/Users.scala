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
      val githubUser = GithubUser.findByLogin(login)

      Async {
        // Could be written two ways:
        // The idiomatic Scala way
        githubUser.map {
          _.map( user => Ok(user.asJson) )
           .getOrElse(NotFound)
        }
        // or using pattern matching
//        githubUser.map(_ match {
//          case Some(user) => Ok(user.asJson)
//          case None => NotFound
//        })
      }

  }

}
