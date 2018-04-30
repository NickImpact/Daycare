-- GTS MySQL Schema

CREATE TABLE `{prefix}ranches` (
  `uuid`      VARCHAR(36) NOT NULL,
  `ranch`     MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;

CREATE TABLE `{prefix}npcs` (
  `uuid`   VARCHAR(36)  NOT NULL,
  `name`   VARCHAR(100) NOT NULL,
  PRIMARY KEY (`uuid`)
) DEFAULT CHARSET = utf8;