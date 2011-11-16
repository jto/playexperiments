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
  description               varchar(255) not null,
  repo                      varchar(255) not null,
  score                     int not null,
  validated                 boolean not null,
  image                     varchar(255) not null,
  author_email              varchar(255) not null,
  url                       varchar(255),
  foreign key(author_email) references author(email) on delete cascade
) engine = innodb character set utf8 collate utf8_general_ci;

# create sequence project_seq start with 1000;

# --- !Downs

drop database playexperiments;
drop table if exists project;
drop table if exists author;
# drop sequence if exists project_seq;
