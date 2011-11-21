package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Author(email: String, name: String, url: Option[String])

object Author {

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[String]("author.email") ~/
    get[String]("author.name") ~/
    get[Option[String]]("author.url") ^^ {
      case email ~ name ~ url => Author(email, name, url)
    }
  }

  // -- Queries

  /**
   * Retrieve a User from email.
   */
  def findByEmail(email: String): Option[Author] = {
    DB.withConnection { implicit connection =>
      SQL("select * from authors where email = {email}").on(
        'email -> email
      ).as(Author.simple ?)
    }
  }

  /**
   * Retrieve all users.
   */
  def findAll: Seq[Author] = {
    DB.withConnection { implicit connection =>
      SQL("select * from user").as(Author.simple *)
    }
  }

  /**
   * Create a User.
   */
  def create(author: Author): Author = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert ignore into author set
            email = {email},
            name = {name},
            url = {url}
        """
      ).on(
        'email -> author.email,
        'name -> author.name,
        'url -> author.url
      ).executeUpdate()

      author
    }
  }

}
