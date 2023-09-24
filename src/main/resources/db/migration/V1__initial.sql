create table `users`
(
    uuid          varchar(36)  not null
        primary key,
    first_name    varchar(64)  not null,
    last_name     varchar(64)  not null,
    email_address varchar(64)  not null,
    password      varchar(128) not null,
    street_name   varchar(128) not null,
    house_number  int          not null,
    postal_code   varchar(8)   not null,
    city          varchar(64)  not null,
    country       varchar(64)  not null,
    latitude      float        not null,
    longitude     float        not null,
    constraint email_address__index
        unique (email_address)
);


create table `cars`
(
    uuid          varchar(36)  not null
        primary key,
    owner_uuid    varchar(36)  not null,
    brand_name    varchar(64)  not null,
    model_name    varchar(64)  not null,
    license_plate varchar(64)  null,
    color         varchar(64)  not null,
    fuel_type     varchar(128) not null,
    rate_per_hour int          not null,
    status        varchar(8)   not null,
    INDEX owner_uuid_index (owner_uuid),
    FOREIGN KEY (owner_uuid)
        REFERENCES `users` (`uuid`)
        ON DELETE CASCADE
);


create table `reservations`
(
    uuid               varchar(36) not null
        primary key,
    rentor_uuid        varchar(36) not null,
    rentee_uuid        varchar(64) not null,
    rented_car_uuid    varchar(32) not null,
    start_datetime_utc datetime    not null,
    end_datetime_utc   datetime    not null,
    total_price        int         not null,
    FOREIGN KEY (rentor_uuid)
        REFERENCES `users` (`uuid`)
        ON DELETE NO ACTION,

    FOREIGN KEY (rentee_uuid)
        REFERENCES `users` (`uuid`)
        ON DELETE NO ACTION
);


create table `photos`
(
    `uuid` varchar(36) not null primary key,
    `car_uuid` varchar(36) not null,
    `index` int not null default 0,
    foreign key (`car_uuid`)
        references `cars`(`uuid`)
        on delete cascade
)
