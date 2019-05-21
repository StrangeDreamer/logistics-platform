create table bank
(
  id     VARCHAR(20) auto_increment
    primary key,
  amount DOUBLE(22) not null
);

create unique index bank_id_uindex
  on bank (id);

create table guarantor
(
  id     VARCHAR(20) not null
    primary key,
  amount DOUBLE(22)  not null
  comment '担保金额'
);

create unique index guarantor_id_uindex
  on guarantor (id);

create table reciever
(
  id          VARCHAR(20) not null
    primary key,
  isActivated TINYINT(3)  null
);

create unique index reciever_id_uindex
  on reciever (id);

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
  id          VARCHAR(20) not null
    primary key,
  isActivated TINYINT(3)  null
);

create table cargo
(
  id           VARCHAR(20)    not null
    primary key,
  shipper_id   VARCHAR(20)    null,
  reciever_id  VARCHAR(20)    null,
  start_time   TIMESTAMP(19)  null,
  cargo_status INT(10)        null,
  price        DECIMAL(10, 2) null,
  constraint cargo_shippe_rid
  foreign key (shipper_id) references shipper (id),
  constraint cargo_reciever_id
  foreign key (reciever_id) references reciever (id)
);

create unique index cargo_id_uindex
  on cargo (id);

create index cargo_reciever_id
  on cargo (reciever_id);

create index cargo_shippe_rid
  on cargo (shipper_id);

create unique index shipper_id_uindex
  on shipper (id);

create table trunk
(
  id VARCHAR(20) not null
    primary key
);

create table cargo_order
(
  id         BIGINT(19) auto_increment
    primary key,
  cargo_id   VARCHAR(20)    null,
  cost_price DECIMAL(10, 2) null,
  trunk_id   VARCHAR(20)    null,
  constraint cargo_id
  foreign key (cargo_id) references cargo (id),
  constraint cargo_order_trunk_id
  foreign key (trunk_id) references trunk (id)
);

create index cargo_id
  on cargo_order (cargo_id);

create unique index cargo_order_id_uindex
  on cargo_order (id);

create index cargo_order_trunk_id
  on cargo_order (trunk_id);

create unique index trunk_trunkId_uindex
  on trunk (id);


