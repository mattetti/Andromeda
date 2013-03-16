package models

import play.api.libs.ws._
import play.api.libs.ws.WS
import java.util.concurrent.TimeUnit
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._



/**
 * Created with IntelliJ IDEA.
 * User: mattetti
 * Date: 3/16/13
 * Time: 1:36 AM
 * To change this template use File | Settings | File Templates.
 */
trait ApiFetcher {
  def get(url: String, timeout: Int = 1) =
    WS.url(url).get().orTimeout("Timeout", timeout, TimeUnit.SECONDS ).map { r => apiResponseHandler(r) }

  def apiResponseHandler(response: Either[Response, String]) = {
    response match {
      // Left and Right and used to define what Type is returned by the Timeout
      case Left(resp) => {
        if (resp.status == 200){
          // TODO check if the response is a json response
          Option(resp.json)
        } else {
          val debuggedResponse = resp.body.toString()
          play.Logger.error(s"Fetching ${resp} failed: $debuggedResponse")
          None
        }
      }
      case _ => {
        play.Logger.error(s"Fetching failed: timed out.")
        None
      }
    }
  }

}
