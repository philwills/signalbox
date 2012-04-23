package controllers

import play.api._
import libs.concurrent.Promise
import play.api.mvc._
import github._

object Signalbox extends Controller {
  
  def index = Action { implicit request =>
    Github.withAccessToken { implicit token =>
      Async {
        val repositoriesPromise = Github.repos flatMap { repositories =>
          val repos = for (repo <- repositories) yield {
            repo.withBranchesAndCommits
          }
          Promise.sequence(repos)
        }
        repositoriesPromise map {repositories =>
          Ok(views.html.Github.list(repositories))}
      }
    }
  }
}