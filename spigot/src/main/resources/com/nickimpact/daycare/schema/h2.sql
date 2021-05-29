CREATE TABLE `{prefix}ranch` (
  `owner`   VARCHAR(36) NOT NULL,
  `ranch`   VARCHAR(36) NOT NULL,
  `stats`   CLOB        NOT NULL,
  PRIMARY KEY (`owner`)
);

CREATE TABLE `{prefix}pens` (
  `ranch`   VARCHAR(36) NOT NULL,
  `pen`     VARCHAR(36) NOT NULL,
  `id`      INTEGER     NOT NULL,
  PRIMARY KEY (`ranch`, `pen`),
  FOREIGN KEY (`ranch`) REFERENCES `{prefix}ranch`(`ranch`)
);

CREATE TABLE `{prefix}pen` (
  `pen`         VARCHAR(36) NOT NULL,
  `slot1`       CLOB,
  `slot2`       CLOB,
  `egg`         CLOB,
  `unlocked`    TINYINT     NOT NULL,
  `dateUnlock`  TIMESTAMP,
  `settings`    CLOB        NOT NULL,
  `stage`       VARCHAR(50),
  FOREIGN KEY (`pen`) REFERENCES `{prefix}pens`(`pen`)
);

CREATE TABLE `{prefix}npcs` (
  `uuid`   VARCHAR(36)  NOT NULL,
  `name`   VARCHAR(100) NOT NULL,
);