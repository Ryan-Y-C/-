create table `order`
(
    id              bigint primary key auto_increment,
    user_id         bigint,
    total_price     decimal,
    address         varchar(1024),
    express_company varchar(16),
    express_id      varchar(128),
    status          varchar(16),
    created_at      timestamp not null default now(),
    updated_at      timestamp not null default now()
);
create table `order_goods`
(
    id       bigint primary key auto_increment,
    goods_id bigint,
    number   decimal
)