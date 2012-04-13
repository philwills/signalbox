package controllers

import play.api._
import play.api.mvc._
import github.Github

object Signalbox extends Controller {
  
  def index = Action { implicit request =>
    Github.withAccessToken { implicit token =>
      Async {
        Github.list map { repos =>
          Ok(views.html.Github.list(repos))
        }
      }
    }
  }
  
}