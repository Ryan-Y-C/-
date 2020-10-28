create table goods
(
    id          bigint primary key auto_increment,
    shop_id     bigint,
    name        varchar(100),
    description varchar(1024),
    details     text,
    img_url     varchar(1024),
    price       decimal,
    stock       int       not null default 0,
    status      varchar(16),-- "ok" 正常 'deleted' 已经删除 逻辑删除
    created_at  timestamp not null default now(),
    updated_at  timestamp not null default now()
)