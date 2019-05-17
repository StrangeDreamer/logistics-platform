create table cargo
(
  id               VARCHAR(20) not null
    primary key,
  needed_carrytype INT(10)     null,
  volume           INT(10)     null
  comment '货物体积',
  weight           INT(10)     null
  comment '货物重量',
  shipper_id       VARCHAR(20) null,
  constraint cargo_shippe_rid
  foreign key (shipper_id) references shipper (id)
);

create unique index cargo_id_uindex
  on cargo (id);

create index cargo_shippe_rid
  on cargo (shipper_id);


