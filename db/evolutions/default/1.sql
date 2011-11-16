# --- First database schema

# --- !Ups

create table user (
  email                     varchar(255) not null primary key,
  name                      varchar(255) not null,
  password                  varchar(255) not null
);

create table project (
  id                        bigint not null primary key,
  name                      varchar(255) not null,
  description               varchar(255) not null
);

create sequence project_seq start with 1000;

create table project_member (
  project_id                bigint not null,
  user_email                varchar(255) not null,
  foreign key(project_id)   references project(id) on delete cascade,
  foreign key(user_email)   references user(email) on delete cascade
);

# --- !Downs

drop table if exists project_member;
drop table if exists project;
drop sequence if exists project_seq;
drop table if exists user;
