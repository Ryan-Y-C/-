create table user(
    id bigint primary key auto_increment,
    name varchar(100),
    tel varchar(100) unique ,
    avatar_url varchar(10240),
    created_at datetime,
    updated_at datetime
)