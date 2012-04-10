package controllers

import play.api.libs.ws.WS
import play.api.mvc._

object Github extends Controller {

  val clientId = "1e6477b2d1ed022cee4b"
  val secretKey = ""
  
  def list = Action { implicit request =>
    withGithubToken { token =>
      Async {
        WS.url("https://api.github.com/orgs/%s/repos" format ("guardian"))
          .withQueryString("access_token" -> token).get(
        ) map { response =>
          Ok(views.html.Github.list(response.json \\ "name" map (_.as[String])))
        }
      }
    }
  }

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
        val resultMap = PairParser.parse(response.body)
        Redirect(request.session.get("return_uri").getOrElse("/")).
          withSession("github_token" -> resultMap("access_token"))
      }
    }
  }

  def withGithubToken(block: String => Result)(implicit request: Request[AnyContent]) = {
    request.session.get("github_token") map { token =>
      block(token)
    } getOrElse {
      Redirect(routes.Github.authenticate).withSession("return_uri" -> request.uri)
    }
  }
}

object PairParser {
  def parse(pairString: String) = {
    Map(pairString.split("&") map { x =>
      val pairs = x.split("=")
      (pairs(0) -> pairs(1))
    } : _*)
  }
}
