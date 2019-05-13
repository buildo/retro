create table loginauthdomain (
    id serial,
    ref text not null,
    username text not null,
    passwordhash text not null,
    primary key (id),
    constraint loginauthdomain_username_index unique (username)
);

create table accesstokenauthdomain (
    id serial,
    ref text not null,
    token text not null,
    expiresat timestamptz not null,
    primary key (id),
    constraint accesstokenauthdomain_token_index unique (token)
);
