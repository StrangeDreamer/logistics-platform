create table reciever
(
  id          VARCHAR(20) not null
    primary key,
  isActivated TINYINT(3)  null
);

create unique index reciever_id_uindex
  on reciever (id);


