package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.validation.Constraints._

import anorm._

import models._

object Application extends Controller {


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
      "project.description" -> requiredText
    )
  )

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def saveProject = Action{ implicit request =>
    projectForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case (id, description, name) =>
          val p = Project.create(Project(id, name, description))
          Ok(p.toString)
      }
    )
  }

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
}