create table bank_account
(
  count            int auto_increment
    primary key,
  bank_account_log text         null,
  available_money  double       not null,
  created_date     datetime     not null,
  id               int          not null,
  modify_time      datetime     not null,
  money            double       not null,
  type             varchar(255) null
);

create table bid
(
  id           int auto_increment
    primary key,
  bid_price    double   not null,
  cargo_id     int      not null,
  created_time datetime not null,
  truck_id     int      not null
);

create table cargo
(
  id             int auto_increment
    primary key,
  abnormal       bit          not null,
  bid_end_time   datetime     null,
  bid_price      double       not null,
  bid_start_time datetime     null,
  created_time   datetime     not null,
  departure      varchar(255) null,
  destination    varchar(255) null,
  freight_fare   double       not null,
  insurance      int          not null,
  limited_time   datetime     null,
  overtime       bit          not null,
  position       varchar(255) null,
  pre_cargo_id   int          null,
  pre_fare       double       not null,
  receiver_id    int          not null,
  remarks        varchar(255) null,
  shipper_id     int          not null,
  status         int          not null,
  truck_id       int          not null,
  type           varchar(255) null,
  volume         double       not null,
  weight         double       not null
);

create table inspection
(
  id                int auto_increment
    primary key,
  cargo_id          int      not null,
  created_time      datetime not null,
  inspection_result int      not null,
  timeout_period    int      not null
);

create table insurance_account
(
  count                 int auto_increment
    primary key,
  insurance_account_log text         null,
  money                 double       not null,
  available_money       double       not null,
  created_date          datetime     not null,
  id                    int          not null,
  modify_time           datetime     not null,
  type                  varchar(255) null
);

create table platform
(
  id                      int auto_increment
    primary key,
  biding_duration         int      not null,
  bonus_max_ratio_in_fare double   not null,
  create_time             datetime not null,
  exhibition_fee          int      not null,
  lowest_bid_price_ratio  double   not null,
  over_time_fee_ratio     double   not null,
  platform_profit_ratio   double   not null,
  shipper_profit_ratio    double   not null,
  truck_profit_ratio      double   not null,
  withdraw_fee_ratio      double   not null
);

create table receiver
(
  id                   int auto_increment
    primary key,
  created_date         datetime     not null,
  id_gongsitongyidaima varchar(255) null,
  idgerenshenfenzheng  varchar(255) null,
  modify_time          datetime     not null,
  name                 varchar(255) null,
  occupation           varchar(255) null,
  tel_number           varchar(255) null
);

create table shipper
(
  id                   int auto_increment
    primary key,
  activated            bit          not null,
  bank_id              varchar(255) null,
  created_date         datetime     not null,
  id_gongsitongyidaima varchar(255) null,
  idgerenshenfenzheng  varchar(255) null,
  modify_time          datetime     not null,
  name                 varchar(255) null,
  occupation           varchar(255) null,
  ranking              double       not null,
  tel_number           varchar(255) null
);

create table truck
(
  id                   int auto_increment
    primary key,
  activated            bit          not null,
  available_volume     double       not null,
  available_weight     double       not null,
  bank_id              varchar(255) null,
  created_date         datetime     not null,
  id_gongsitongyidaima varchar(255) null,
  id_jiashizheng       varchar(255) null,
  id_xingshizheng      varchar(255) null,
  idgerenshenfenzheng  varchar(255) null,
  insurance_id         varchar(255) null,
  modify_time          datetime     not null,
  name                 varchar(255) null,
  position             varchar(255) null,
  power                int          not null,
  ranking              double       not null,
  tel_number           varchar(255) null,
  type                 varchar(255) null
);


