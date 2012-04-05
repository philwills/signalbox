package controllers

import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.libs.ws.WS

object Github extends Controller {

  val clientId = "1e6477b2d1ed022cee4b"
  val secretKey = "xxxxx"

  def authenticate = Action { request =>
    Redirect("https://github.com/login/oauth/authorize", Map("client_id" -> Seq(clientId)))
  }

  def callback = Action { request =>
    Async {
      val code: String = request.queryString("code").head

      WS.url("https://github.com/login/oauth/access_token").post(Map(
        "client_id" -> Seq(clientId),
        "client_secret" -> Seq(secretKey),
        "code" -> Seq(code)
      )) map { response =>
        val results = for {
          pairs <- response.body.split("&")
          keyVals <- pairs.split("=")
        } yield (keyVals(0) -> keyVals(1))
        val resultMap: Map[String, String] = Map(results.toSeq: _*)
        Ok(resultMap("access_token"))
      }
    }
  }
}
