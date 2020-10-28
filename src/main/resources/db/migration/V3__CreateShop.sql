create table shop
(
    id            bigint primary key auto_increment,
    name          varchar(100),
    description   varchar(1024),
    img_url       varchar(1024),
    owner_user_id bigint,
    status        varchar(16),
    created_at    timestamp not null default now(),
    updated_at    timestamp not null default now()
)