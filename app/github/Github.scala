package github

import play.api.libs.ws.WS
import play.api.mvc._
import com.typesafe.config.ConfigFactory

object Github extends Controller {

  lazy val config = ConfigFactory.load();
  lazy val clientId = config.getString("github.clientid")
  lazy val secretKey = config.getString("github.secretkey")

  def list(implicit token: AccessToken) = {
    WS.url("https://api.github.com/orgs/%s/repos" format ("guardian"))
      .withQueryString("access_token" -> token.value).get(
    ) map {
      response =>
        response.json \\ "name" map (_.as[String])
    }
  }

  def authenticate = Action {
    request =>
      Redirect("https://github.com/login/oauth/authorize", Map("client_id" -> Seq(clientId)))
  }

  def callback = Action {
    request =>
      Async {
        val code: String = request.queryString("code").head

        WS.url("https://github.com/login/oauth/access_token").post(Map(
          "client_id" -> Seq(clientId),
          "client_secret" -> Seq(secretKey),
          "code" -> Seq(code)
        )) map {
          response =>
            val resultMap = PairParser.parse(response.body)
            Redirect(request.session.get("return_uri").getOrElse("/")).
              withSession("github_token" -> resultMap("access_token"))
        }
      }
  }

  def withAccessToken(block: AccessToken => Result)(implicit request: Request[AnyContent]) = {
    request.session.get("github_token") map {
      token =>
        block(AccessToken(token))
    } getOrElse {
      Redirect(routes.Github.authenticate).withSession("return_uri" -> request.uri)
    }
  }
}

case class Repository(name: String)
case class Branch(name: String)
case class AccessToken(value: String)

object PairParser {
  def parse(pairString: String) = {
    Map(pairString.split("&") map {
      x =>
        val pairs = x.split("=")
        (pairs(0) -> pairs(1))
    }: _*)
  }
}


