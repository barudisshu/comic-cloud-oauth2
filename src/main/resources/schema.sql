drop database `comic-cloud-oauth2`;
create database `comic-cloud-oauth2`;

use `comic-cloud-oauth2`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for accounts
-- ----------------------------
DROP TABLE IF EXISTS `accounts`;
create table `accounts`
(
  `uid`        VARCHAR(64)  NOT NULL PRIMARY KEY UNIQUE,
  `username`   VARCHAR(500) NOT NULL,
  `password`   VARCHAR(500) NOT NULL,
  `salt`       VARCHAR(500) NOT NULL UNIQUE,
  `email`      VARCHAR(100) NOT NULL,
  `phone`      VARCHAR(20),
  `created_at` timestamp    not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create unique index `account_salt_idx` on `accounts` (`salt`);
create unique index `account_username_idx` on `accounts` (`username`);

-- ----------------------------
-- Table structure for clients
-- ----------------------------
DROP TABLE IF EXISTS `clients`;
create table `clients`
(
  `uid`           VARCHAR(64)  NOT NULL PRIMARY KEY UNIQUE,
  `owner_id`      VARCHAR(64)  NOT NULL,
  `grant_type`    VARCHAR(20)  NOT NULL,
  `client_id`     VARCHAR(100) NOT NULL UNIQUE,
  `client_secret` VARCHAR(100) NOT NULL UNIQUE,
  `redirect_uri`  VARCHAR(2000),
  `created_at`    timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_0900_ai_ci;

create unique index `client_client_id_idx` on `clients` (`client_id`);
create unique index `client_client_secret_idx` on `clients` (`client_secret`);
SET FOREIGN_KEY_CHECKS=1;