create table bank_account
(
  id               INT(10) auto_increment
    primary key,
  disposable_money DOUBLE(22) not null,
  frozen_money     DOUBLE(22) not null,
  money            DOUBLE(22) not null
);

create table bid
(
  id           INT(10) auto_increment
    primary key,
  bidding_time DATETIME(19) null,
  biding_money DOUBLE(22)   not null,
  cargo_id     INT(10)      not null,
  truck_id     INT(10)      not null
);

create table cargo
(
  id           INT(10) auto_increment
    primary key,
  departure    VARCHAR(255)  null,
  destination  VARCHAR(255)  null,
  freight_fare DOUBLE(22)    not null,
  receiver_id  INT(10)       not null,
  shipper_id   INT(10)       not null,
  start_time   TIMESTAMP(19) not null,
  volume       DOUBLE(22)    not null,
  weight       DOUBLE(22)    not null
);

create table cargo_order
(
  id            INT(10) auto_increment
    primary key,
  cargo_id      INT(10)        not null,
  complete_time DATETIME(19)   null,
  cost_price    DECIMAL(19, 2) null,
  is_abnormal   BIT(1)         not null,
  is_overtime   BIT(1)         not null,
  order_price   DOUBLE(22)     not null,
  register_time DATETIME(19)   null,
  status        INT(10)        not null,
  truck_id      INT(10)        not null
);

create table cargo_order_lite
(
  id         INT(10) auto_increment
    primary key,
  cargo_id   INT(10)        not null,
  cost_price DECIMAL(19, 2) null,
  truck_id   INT(10)        not null
);

create table guarantor_account
(
  id               INT(10) auto_increment
    primary key,
  disposable_money DOUBLE(22) not null,
  frozen_money     DOUBLE(22) not null,
  money            DOUBLE(22) not null
);

create table receiver
(
  id            INT(10) auto_increment
    primary key,
  name          VARCHAR(255) null,
  register_time DATETIME(19) null
);

create table seckill
(
  seckill_id  BIGINT(19) auto_increment
  comment '商品ID'
    primary key,
  title       VARCHAR(1000)                               null
  comment '商品标题',
  image       VARCHAR(1000)                               null
  comment '商品图片',
  price       DECIMAL(10, 2)                              null
  comment '商品原价格',
  cost_price  DECIMAL(10, 2)                              null
  comment '商品秒杀价格',
  stock_count BIGINT(19)                                  null
  comment '剩余库存数量',
  start_time  TIMESTAMP(19) default '1970-02-01 00:00:01' not null
  comment '秒杀开始时间',
  end_time    TIMESTAMP(19) default '1970-02-01 00:00:01' not null
  comment '秒杀结束时间',
  create_time TIMESTAMP(19) default CURRENT_TIMESTAMP     not null
  comment '创建时间'
);

create index idx_create_time
  on seckill (end_time);

create index idx_end_time
  on seckill (end_time);

create index idx_start_time
  on seckill (start_time);

create table seckill_order
(
  seckill_id  BIGINT(19)                              not null
  comment '秒杀商品ID',
  money       DECIMAL(10, 2)                          null
  comment '支付金额',
  user_phone  BIGINT(19)                              not null
  comment '用户手机号',
  create_time TIMESTAMP(19) default CURRENT_TIMESTAMP not null
  comment '创建时间',
  state       TINYINT(3) default -1                   not null
  comment '状态：-1无效 0成功 1已付款',
  primary key (seckill_id, user_phone)
);

create table shipper
(
  id            INT(10) auto_increment
    primary key,
  name          VARCHAR(255)                            null,
  register_time TIMESTAMP(19) default CURRENT_TIMESTAMP not null
);

create table truck
(
  id               INT(10) auto_increment
    primary key,
  available_volume DOUBLE(22)   not null,
  available_weight DOUBLE(22)   not null,
  name             VARCHAR(255) null,
  register_time    DATETIME(19) null,
  type             INT(10)      not null
);


