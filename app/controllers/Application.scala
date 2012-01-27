package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data._
import play.api.data.Forms._

import anorm._

import models._

object Forms{

  val projectForm = Form(
    mapping(
      "project.id" -> ignored(NotAssigned:Pk[Long]),
      "project.name" -> nonEmptyText,
      "project.description" -> nonEmptyText,
      "project.repo" -> nonEmptyText,
      "project.score" -> ignored(0),
      "project.validated" -> ignored(false),
      "project.image" -> text,
      "author.email" -> ignored(Nil:List[Author]),
      "project.url" -> optional(text)
    )(Project.apply)(Project.unapply)
  )

  val authorForm = Form(
    mapping(
      "author.email" -> nonEmptyText,
      "author.name" -> nonEmptyText,
      "author.url" -> optional(text)
    )(Author.apply)(Author.unapply)
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
                val p = Project.create(project.copy(authors = List(auth)))
                Home.flashing("success" -> "Experiment %s has been updated".format(p.name))
            }
          )
        }
      }
    )

  }

  def validate(id: Long) = Action{ implicit request =>
    Play.current.configuration.getString("admin.secret").map { confsecret =>
      val secret = request.body.asFormUrlEncoded.get("secret").head
      confsecret match{
        case s if s == secret => Project.validate(id); Ok("Validated")
        case _ => Forbidden
      }
    }.getOrElse(NotFound)
  }

  def vote(id: Long) = Action{ implicit request =>
    Form("score" -> number(0, 5)).bindFromRequest.fold(
      errors => BadRequest,
      score => {
        Project.vote(id, score)
        Ok("Upvoted :)")
      }
    )
  }

}
