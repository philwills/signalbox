package github

import play.api.libs.ws.WS
import play.api.mvc._
import com.typesafe.config.ConfigFactory
import net.liftweb.json._
import net.liftweb.json
import play.api.Configuration._
import java.io.File
import play.api.{Play, Configuration}
import play.api.libs.concurrent.Promise

object Github extends Controller {
  import Play.current

  implicit val formats = DefaultFormats

  lazy val config = Play.configuration.getConfig("github").get
  lazy val githubConf = Configuration(ConfigFactory.parseFileAnySyntax(new File("/signalbox/github.conf")))
  lazy val amalgamatedConfig = config ++ githubConf

  lazy val clientId = amalgamatedConfig.getString("clientid").get
  lazy val secretKey = amalgamatedConfig.getString("secretkey").get

  def repos(implicit token: AccessToken) = {
    getFromAPI("orgs/%s/repos" format ("guardian")) { json =>
      json.extract[List[Repository]]
    }
  }

  def branches(repo: Repository)(implicit token: AccessToken) = {
    getFromAPI("repos/%s/%s/branches" format ("guardian", repo.name)) { json =>
      json.extract[List[Branch]]
    }
  }

  def commits(repo: Repository, branch: Option[Branch] = None)(implicit token: AccessToken) = {
    getFromAPI("repos/%s/%s/commits" format ("guardian", repo.name),
      "sha" -> (branch map (_.name) getOrElse ("master"))) { json =>
      json.extract[List[Commit]]
    }
  }
  
  def getFromAPI[T](path: String, params: (String, String)*)(action: JValue => T)(implicit token: AccessToken) = {
    WS.url("https://api.github.com/%s" format (path))
      .withQueryString(params + "access_token" -> token.value).get(
    ) map {
      response => {
        action(json.parse(response.body))
      }
    }
  }

  def authenticate = Action { request =>
    Redirect("https://github.com/login/oauth/authorize", Map("client_id" -> Seq(clientId)))
  }

  def callback(code: String) = Action {
    request =>
      Async {
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

case class Repository(name: String, branches: Seq[Branch] = Nil) {
  def withBranches(implicit token: AccessToken) : Promise[Repository] = {
    for {
      branchesForRepo <- Github.branches(this)
    } yield {
      copy(branches = branchesForRepo)
    }
  }
  def withBranchesAndCommits(implicit token: AccessToken) : Promise[Repository] = {
    val repoWithBranchesPromise = withBranches
    repoWithBranchesPromise flatMap { repo =>
      val branchesPromise = Promise.sequence(for (branch <- repo.branches) yield {
        for {
          commitsForBranch <- Github.commits(repo, Some(branch))
        } yield {
          branch.copy(commits = commitsForBranch)
        }
      })
      for (branchesWithCommits <- branchesPromise) yield repo.copy(branches = branchesWithCommits)
    }
  }
}
case class Branch(name: String, commits: Seq[Commit] = Nil)
case class Commit(sha: String)

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


