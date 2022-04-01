drop database if exists poadevice;
drop user if exists poadevice;
create user poadevice with password 'password';
create database poadevice with template=template0 owner=poadevice;
\connect poadevice;
alter default privileges grant all on tables to poadevice;
alter default privileges grant all on sequences to poadevice;

create table poa(
    id integer primary key not null,
    destinationNetworkId text not null
);

create sequence poa_seq increment 1 start 1;
