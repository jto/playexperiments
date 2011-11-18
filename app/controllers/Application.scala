package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.validation.Constraints._

import anorm._

import models._

object Forms{

  val projectForm = Form(
    of(Project.apply _)(
      "project.id" -> ignored(NotAssigned),
      "project.name" -> requiredText,
      "project.description" -> requiredText,
      "project.repo" -> requiredText,
      "project.score" -> ignored(0),
      "project.validated" -> ignored(false),
      "project.image" -> text,
      "author.email" -> ignored(Nil),
      "project.url" -> optional(text)
    )
  )

  val authorForm = Form(
    of(Author.apply _)(
      "author.email" -> requiredText,
      "author.name" -> requiredText,
      "author.url" -> optional(text)
    )
  )

}

object Application extends Controller {

  import Forms._

  val Home = Redirect(routes.Application.index)

  def index = Action {
    Ok(views.html.index(Project.findValidated))
  }

  def get(id: Long) = Action {
    Project.findById(id)
           .map(p => Ok(views.html.experiment(p)))
           .getOrElse(NotFound)
  }

  def all = Action {
    Ok(views.html.index(Project.findAll))
  }

  def submit = Action {
    // TODO
    Ok(views.html.submitProject(projectForm))
  }

  def save = Action{ implicit request =>
    // XXX: don't like this {{{ }}}
    authorForm.bindFromRequest.fold(
      errors => BadRequest,
      {
        case auth: Author => {
          projectForm.bindFromRequest.fold(
            errors => BadRequest(views.html.submitProject(errors)),
            {
              case project: Project =>
                // It's unlikely to fail, but still, should be transactionnal
                val a = Author.create(auth)
                val p = Project.create(project)
                Home.flashing("success" -> "Experiment %s has been updated".format(p.name))
            }
          )
        }
      }
    )

  }

  def validate(id: Long) = Action{ implicit request =>
    Play.current.configuration.getString("admin.secret").map { confsecret =>
      val secret = request.body.urlFormEncoded("secret").head
      confsecret match{
        case s if s == secret => Project.validate(id); Ok("Validated")
        case _ => Forbidden
      }
    }.getOrElse(NotFound)
  }

  def upvote(id: Long) = Action{
    Project.upvote(id)
    Ok("Upvoted :)")
  }

}
