package wrappers

import play.api.libs.ws._
import play.api.libs.ws.WS
import play.api.libs.concurrent._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue


/**
 * Created with IntelliJ IDEA.
 * User: mattetti
 * Date: 3/16/13
 * Time: 1:36 AM
 * To change this template use File | Settings | File Templates.
 */
trait ApiFetcher {

  /**
   *
   * @param url the full url to fetch, including query params.
   * @param timeout After how long should the query timeout.
   * @param fallback What value should be returned in case of a timeout.
   * @return The returned value should be an Option containing the json response, none or fallback.
   */
  def get(url: String, timeout: Long = 1000, fallback: Option[JsValue] = None) =
    WS.url(url).get().orTimeout(fallback, timeout).map( apiResponseHandler )

  def apiResponseHandler(response: Either[Response, Option[JsValue]]): Option[JsValue] = {
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
      case Right(fallsbackResp) => {
        play.Logger.error(s"Fetching failed: timed out.")
        fallsbackResp
      }
    }
  }

}
