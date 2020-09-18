create table users(
id integer auto_increment,
name varchar(50),
password varchar(500),
email varchar(100),
primary key (id));

create table wallet(
id integer auto_increment,
name varchar(60),
value numeric(10,2),
primary key (id));

create table users_wallet(
id integer auto_increment,
wallet integer,
users integer,
primary key(id),
foreign key(users) references users(id),
foreign key(wallet) references wallet(id));

create table wallet_items(
id integer auto_increment,
wallet integer,
date date,
type varchar(2),
description varchar(500),
value numeric(10,2),
primary key(id),
foreign key(wallet) references wallet(id)
);