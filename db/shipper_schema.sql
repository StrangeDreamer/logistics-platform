create table shipper
(
  id          VARCHAR(20) not null
    primary key,
  isActivated TINYINT(3)  null
);

create unique index shipper_id_uindex
  on shipper (id);


