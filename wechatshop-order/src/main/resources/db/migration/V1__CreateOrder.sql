create table `order`
(
    id              bigint primary key auto_increment,
    user_id         bigint,
    total_price     bigint,
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
    order_id bigint,
    number   bigint
);
INSERT INTO `order` (ID, USER_ID, TOTAL_PRICE, ADDRESS, EXPRESS_COMPANY, EXPRESS_ID, STATUS)
VALUES (1, 1, 1400, '火星', '顺丰', '运单1234567', 'delivered');

INSERT INTO ORDER_GOODS(GOODS_ID, ORDER_ID, NUMBER)
VALUES (1, 1, 5),
       (2, 1, 9);