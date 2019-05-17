create table guarantor
(
  id     VARCHAR(20) not null
    primary key,
  amount DOUBLE(22)  not null
  comment '担保金额'
);

create unique index guarantor_id_uindex
  on guarantor (id);


