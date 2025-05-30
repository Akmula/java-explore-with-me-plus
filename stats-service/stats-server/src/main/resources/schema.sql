DROP TABLE IF EXISTS hits;

CREATE TABLE IF NOT EXISTS hits
(
    id        INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    app       VARCHAR(255) NOT NULL,
    uri       VARCHAR(512)                NOT NULL,
    ip        VARCHAR(20)                 NOT NULL,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL
);