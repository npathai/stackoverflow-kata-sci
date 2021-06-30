create table if not exists users (
    id varchar(255) not null,
    username varchar(255) not null,
    email varchar(255) not null,
    reputation bigint not null
);

create table if not exists questions
(
    id    varchar(255)  not null
        primary key,
    title varchar(2000) not null
);
