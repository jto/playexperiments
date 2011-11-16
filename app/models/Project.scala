package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Project(id: Pk[Long],
  name: String,
  description: String,
  repo: String,
  score: Int = 0,
  validated: Boolean = false,
  image: String,
  author: String,
  url: Option[String])

object Project {

  // -- Parsers

  /**
   * Parse a Project from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("project.id") ~/
    get[String]("project.description") ~/
    get[String]("project.name") ~/
    get[Int]("project.score") ~/
    get[Boolean]("project.validated") ~/
    get[String]("project.image") ~/
    get[String]("project.author_email") ~/
    get[Option[String]]("project.url") ~/
    get[String]("project.repo") ^^ {
      case id ~ description ~ name ~ score ~ validated ~ image ~ author ~ url ~ repo => Project(id, name, description, repo, score, validated, image, author, url)
    }
  }

  // -- Queries

  /**
   * Find all projects
   */
  def findAll: Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from project order by score desc").as(Project.simple *)
    }
  }

  def findValidated: Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from project where validated = true order by score desc").as(Project.simple *)
    }
  }

  /**
   * Retrieve a Project from id.
   */
  def findById(id: Long): Option[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from project where validated = true and id = {id}").on(
        'id -> id
      ).as(Project.simple ?)
    }
  }

  /**
   * Delete a project.
   */
  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("delete from project where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }

  /**
   * Create a Project.
   */
  def create(project: Project): Project = {
     DB.withTransaction { implicit connection =>

       // Insert the project
       SQL(
         """
           insert into project values (
             NULL, {name}, {description}, {repo}, {score}, {validated}, {image}, {author}, {url}
           )
         """
       ).on(
         'name -> project.name,
         'description -> project.description,
         'repo -> project.repo,
         'score -> project.score,
         'validated -> project.validated,
         'image -> project.image,
         'author -> project.author,
         'url -> project.url
       ).executeUpdate()

       project.copy(id = Id(SQL("select LAST_INSERT_ID()").as(scalar[Long])))

     }
  }

  /**
   * Upvote a project.
   */
  def upvote(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("update project set score = (score + 1) where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }

  def validate(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("update project set validated = true where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }
}