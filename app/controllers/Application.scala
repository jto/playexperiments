package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.validation.Constraints._

import anorm._

import models._

object Forms{
  val userForm = Form(
    of(
      "user.email" -> requiredText,
      "user.name" -> requiredText,
      "user.password" -> requiredText
    )
  )

  val projectForm = Form(
    of(
      "project.id" -> ignored(NotAssigned),
      "project.name" -> requiredText,
      "project.description" -> requiredText,
      "project.repo" -> requiredText
    )
  )
}

object Application extends Controller {

  import Forms._

  val Home = Redirect(routes.Application.index)

  def index = Action {
    Ok(views.html.index(Project.findValidated))
  }

  def saveProject = Action{ implicit request =>
    projectForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case (id, description, name, repo) =>
          val p = Project.create(Project(id, name, description, repo))
          Home.flashing("success" -> "Experiment %s has been updated".format(p.name))
      }
    )
  }

  def upvote(id: Long) = Action{
    Project.upvote(id)
    Ok("Upvoted :)")
  }


/*
  def saveUser = Action{ implicit request =>
    userForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case (email, name, password) =>
          User.create(User(email, name, password))
          Ok(email + " " + name + " " + password)
      }
    )
  }
*/
}