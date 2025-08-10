ALTER TABLE `account`
  ADD COLUMN  `version` BIGINT NOT NULL DEFAULT 0
  COMMENT '낙관적 락을 위한 버전 컬럼';