# --- First database schema

# --- !Ups

create database if not exists `playexperiments` character set utf8 collate utf8_general_ci;
use `playexperiments`;

create table author (
  email                     varchar(255) not null primary key,
  name                      varchar(255) not null,
  url                       varchar(255)
) engine = innodb character set utf8 collate utf8_general_ci;

create table project (
  id                        bigint not null primary key AUTO_INCREMENT,
  name                      varchar(255) not null,
  description               varchar(5000) not null,
  repo                      varchar(255) not null,
  score                     int not null,
  validated                 boolean not null,
  image                     varchar(255) not null,
  url                       varchar(255)
) engine = innodb character set utf8 collate utf8_general_ci;

create table project_author(
  author_email              varchar(255) not null,
  project_id                bigint not null,
  foreign key(author_email) references author(email) on delete cascade,
  foreign key(project_id) references project(id) on delete cascade
) engine = innodb character set utf8 collate utf8_general_ci;

# create sequence project_seq start with 1000;

insert into author(email, name, url) values("jto@zenexity.com", "Julien Tournay", "http://github.com/jto");
insert into project(id, name, description, repo, score, validated, image, url) values(1, "Play!Experiments", "Awesome project from hackday", "http://github.com/jto/playexperiments", 999, true, "", "http://localhost:9000  ");
insert into project_author(author_email, project_id) values("jto@zenexity.com", 1);
# --- !Downs

# drop database if exists playexperiments;
drop table if exists project;
drop table if exists author;
drop table if exists project_author;
# drop sequence if exists project_seq;
