CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS customer;
CREATE SCHEMA IF NOT EXISTS booking;

CREATE TABLE customer.users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(64) UNIQUE NOT NULL,
    password VARCHAR(64)        NOT NULL,
    name     VARCHAR(64)        NOT NULL
);

CREATE TABLE booking.assets
(
    id           SERIAL PRIMARY KEY,
    guid         UUID UNIQUE NOT NULL DEFAULT uuid_generate_v4(),
    manufacturer VARCHAR(64) NOT NULL,
    model        VARCHAR(64) NOT NULL,
    technology   VARCHAR(256),
    bands2g      VARCHAR(256),
    bands3g      VARCHAR(256),
    bands4g      VARCHAR(256),
    version      integer     NOT NULL DEFAULT 0 -- Version of the asset, used for optimistic locking
);

CREATE TABLE booking.bookings
(
    id         SERIAL PRIMARY KEY,
    guid       UUID UNIQUE                 NOT NULL DEFAULT uuid_generate_v4(),
    user_id    INTEGER NOT NULL REFERENCES customer.users (id),
    asset_id   INTEGER NOT NULL REFERENCES booking.assets (id),
    active     BOOLEAN                     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
-- Only one active booking per asset
CREATE UNIQUE INDEX active_bookings_key ON booking.bookings (asset_id, active) WHERE active;

CREATE OR REPLACE FUNCTION booking.update_updated_at()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$func$
BEGIN
    NEW.updated_at = now() AT TIME ZONE 'utc';
    RETURN NEW;
END;
$func$;

CREATE TRIGGER update_updated_at
    BEFORE UPDATE
    ON booking.bookings
    FOR EACH ROW
EXECUTE PROCEDURE booking.update_updated_at();

CREATE MATERIALIZED VIEW booking.active_bookings AS
SELECT bk.*, ast.guid AS phone_guid, ast.manufacturer, ast.model, usr.username, usr.name
FROM booking.bookings bk
         JOIN booking.assets ast on bk.asset_id = ast.id
         JOIN customer.users usr on bk.user_id = usr.id
WHERE bk.active;

CREATE OR REPLACE FUNCTION booking.update_bookings()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$func$
BEGIN
    REFRESH MATERIALIZED VIEW booking.active_bookings;
    RETURN NULL;
END;
$func$;

CREATE TRIGGER update_bookings
    AFTER INSERT OR UPDATE OR DELETE
    ON booking.bookings
    FOR EACH STATEMENT
EXECUTE PROCEDURE booking.update_bookings();


-- Test data

INSERT INTO customer.users (username, password, name)
VALUES ('test', 'test', 'Test User'),
       ('user', 'password', 'User'),
       ('tester', 'test', 'Tester'),
       ('johndoe', '123456789', 'John Doe');


-- Samsung Galaxy S9
-- 2x Samsung Galaxy S8
-- Motorola Nexus 6
-- Oneplus 9
-- Apple iPhone 13
-- Apple iPhone 12
-- Apple iPhone 11
-- Apple iPhone X
-- Nokia 3310

INSERT INTO booking.assets (manufacturer, model) VALUES
('Samsung', 'Galaxy S9'),
('Samsung', 'Galaxy S8'),
('Samsung', 'Galaxy S8'),
('Motorola', 'Nexus 6'),
('Oneplus', '9'),
('Apple', 'iPhone 13'),
('Apple', 'iPhone 12'),
('Apple', 'iPhone 11'),
('Apple', 'iPhone X'),
('Nokia', '3310');

