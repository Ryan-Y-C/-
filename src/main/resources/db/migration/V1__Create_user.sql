create table user(
    id bigint primary key auto_increment,
    name varchar(100),
    tel varchar(100) unique ,
    avatar_url varchar(1024),
    address varchar(1024),
    created_at datetime not null default now(),
    updated_at datetime not null default now()
)