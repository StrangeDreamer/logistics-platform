create table bank
(
  id     VARCHAR(20) auto_increment
    primary key,
  amount DOUBLE(22) not null
);

create unique index bank_id_uindex
  on bank (id);


