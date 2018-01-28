-- GTS H2 Schema

CREATE TABLE `{prefix}ranches` (
  `uuid`      VARCHAR(36) NOT NULL,
  `ranch`   MEDIUMTEXT  NOT NULL,
  PRIMARY KEY (`uuid`)
);
