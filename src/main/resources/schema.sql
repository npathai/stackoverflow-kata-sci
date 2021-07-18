create table if not exists answers
(
    id varchar(255) not null
        primary key,
    author_id varchar(255) null,
    body varchar(255) null,
    question_id varchar(255) null
);

create table if not exists questions
(
    id varchar(255) not null
        primary key,
    author_id varchar(255) null,
    body varchar(255) null,
    created_at bigint not null,
    title varchar(255) null,
    answer_count int not null
);

create table if not exists tags
(
    id varchar(255) not null
        primary key,
    name varchar(255) null
);

create table if not exists questions_tags
(
    question_id varchar(255) not null,
    tags_id varchar(255) not null,
    constraint FK4u5xv906wfevngoe973bec6u0
        foreign key (question_id) references questions (id),
    constraint FK6u4hn6gnemy7jqlkgeumxwae2
        foreign key (tags_id) references tags (id)
);

create table if not exists users
(
    id varchar(255) not null
        primary key,
    email varchar(255) null,
    reputation bigint not null,
    username varchar(255) null,
    cast_down_votes bigint default 0,
    cast_up_votes bigint default 0
);