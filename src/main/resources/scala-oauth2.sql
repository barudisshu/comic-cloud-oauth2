/*
Navicat MySQL Data Transfer

Source Server         : MySQL - 127.0.0.1
Source Server Version : 80012
Source Host           : localhost:3306
Source Database       : comic

Target Server Type    : MYSQL
Target Server Version : 80012
File Encoding         : 65001

Date: 2018-09-03 18:56:59
*/

drop database `comic`;
create database `comic`;

use `comic`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for ACCOUNTS
-- ----------------------------
DROP TABLE IF EXISTS `ACCOUNTS`;
create table `ACCOUNTS`
(
  `ID`         INTEGER      NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  `UID`        VARCHAR(64)  NOT NULL UNIQUE,
  `USERNAME`   VARCHAR(500) NOT NULL,
  `PASSWORD`   VARCHAR(500) NOT NULL,
  `SALT`       VARCHAR(500) NOT NULL UNIQUE,
  `EMAIL`      VARCHAR(100) NOT NULL,
  `PHONE`      VARCHAR(20),
  `CREATED_AT` timestamp    not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

create unique index `ACCOUNT_SALT_IDX` on `ACCOUNTS` (`SALT`);
create unique index `ACCOUNT_USERNAME_IDX` on `ACCOUNTS` (`USERNAME`);

-- ----------------------------
-- Table structure for CLIENTS
-- ----------------------------
DROP TABLE IF EXISTS `CLIENTS`;
create table `CLIENTS`
(
  `ID`            INTEGER      NOT NULL AUTO_INCREMENT PRIMARY KEY UNIQUE,
  `OWNER_ID`      VARCHAR(64)  NOT NULL UNIQUE,
  `CLIENT_ID`     VARCHAR(100) NOT NULL UNIQUE,
  `CLIENT_SECRET` VARCHAR(100) NOT NULL UNIQUE,
  `REDIRECT_URI`  VARCHAR(2000),
  `CREATED_AT`    timestamp default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_0900_ai_ci;

create unique index `CLIENT_CLIENT_ID_IDX` on `CLIENTS` (`CLIENT_ID`);
create unique index `CLIENT_CLIENT_SECRET_IDX` on `CLIENTS` (`CLIENT_SECRET`);
SET FOREIGN_KEY_CHECKS=1;
