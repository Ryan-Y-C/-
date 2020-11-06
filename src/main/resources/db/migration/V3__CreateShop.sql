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
);
INSERT INTO SHOP (ID, NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS)
VALUES (1, 'shop1', 'desc1', 'url1', 1, 'ok');
INSERT INTO SHOP (ID, NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS)
VALUES (2, 'shop2', 'desc2', 'url2', 1, 'ok');