-- transaction 테이블에 request_id 컬럼 추가
ALTER TABLE `transaction`
  ADD COLUMN `request_id` VARCHAR(64) NULL;

-- request_id 유니크 인덱스 생성 (이미 존재하면 무시)
CREATE UNIQUE INDEX `uk_transaction_request_id` 
ON `transaction` (`request_id`);