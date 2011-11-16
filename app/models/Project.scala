package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Project(id: Pk[Long], description: String, name: String)

object Project {

  // -- Parsers

  /**
   * Parse a Project from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("project.id") ~/
    get[String]("project.description") ~/
    get[String]("project.name") ^^ {
      case id~description~name => Project(id, description, name)
    }
  }

  // -- Queries

  /**
   * Retrieve a Project from id.
   */
  def findById(id: Long): Option[Project] = {
    DB.withConnection { implicit connection =>
      SQL("select * from project where id = {id}").on(
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

       // Get the project id
       val id: Long = project.id.getOrElse {
         SQL("select next value for project_seq").as(scalar[Long])
       }

       // Insert the project
       SQL(
         """
           insert into project values (
             {id}, {name}, {description}
           )
         """
       ).on(
         'id -> id,
         'name -> project.name,
         'description -> project.description
       ).executeUpdate()

       project.copy(id = Id(id))

     }
  }
}