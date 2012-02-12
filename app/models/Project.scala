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
  authors: List[Author],
  url: Option[String])

object Project {

  // -- Parsers

  /**
   * Parse a Project from a ResultSet
   */
  val simple = {
      val rs = 
        (get[Pk[Long]]("project.id") ~
        get[String]("project.description") ~
        get[String]("project.name") ~
        get[Int]("project.score") ~
        get[Boolean]("project.validated") ~
        get[String]("project.image") ~
        get[Option[String]]("project.url") ~
        get[String]("project.repo") ~
        Author.simple).map {
          case id ~ description ~ name ~ score ~ validated ~ image ~ url ~ repo ~ author
            => (id, description, name, score, validated, image, url, repo, author)
        } *;

    rs.map(r =>
        r.groupBy(_._1)
        .flatMap{ case (k, ps) =>
            ps.headOption.map{ p =>
                val (id, description, name, score, validated, image, url, repo, author) = p
                Project(id, name, description, repo, score, validated, image, ps.map(_._9), url)
            }
        }.toList
    )
    
  }

  // -- Queries

  /**
   * Find all projects
   */
  def findAll: Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select * from project
        inner join project_author on project_author.project_id = project.id
        inner join  author on project_author.author_email = author.email
        order by score desc
      """)
      .as(Project.simple)
      
      
    }
  }

  def findValidated: Seq[Project] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select * from project
        inner join project_author on project_author.project_id = project.id
        inner join  author on project_author.author_email = author.email
        where validated = true
        order by score desc
      """).as(Project.simple)
    }
  }

  /**
   * Retrieve a Project from id.
   */
  def findById(id: Long): Option[Project] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select * from project
        inner join project_author on project_author.project_id = project.id
        inner join  author on project_author.author_email = author.email
        where validated = true and id = {id}
      """).on(
        'id -> id
      ).as(Project.simple).headOption
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
       
       project.authors.foreach(Author.create(_))
       SQL(
         """
           insert into project values (
             NULL, {name}, {description}, {repo}, {score}, 0, {validated}, {image}, {url}
           )
         """
       ).on(
         'name -> project.name,
         'description -> project.description,
         'repo -> project.repo,
         'score -> project.score,
         'validated -> project.validated,
         'image -> project.image,
         'url -> project.url
       ).executeUpdate()
       
       val p = project.copy(id = Id(SQL("select LAST_INSERT_ID()").as(scalar[Long].single)))
       
       project.authors.foreach{ a =>
         SQL("""
           insert into project_author(author_email, project_id) values({email}, {pid})
          """).on(
             'email -> a.email,
             'pid -> p.id
           ).executeUpdate()
       }
       p
     }
  }

  /**
   * Upvote a project.
   */
  def vote(id: Long, score: Long) {
    DB.withConnection { implicit connection =>
      SQL("""
          update project set
          score = (score * votecount + {s}) / (votecount + 1),
          votecount = (votecount + 1) 
          where id = {id}
        """).on(
        'id -> id,
        's -> score
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
