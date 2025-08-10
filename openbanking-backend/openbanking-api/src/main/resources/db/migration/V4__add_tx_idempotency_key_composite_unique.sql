-- 기존에 단일 유니크가 있다면 제거하고
ALTER TABLE `transaction` DROP INDEX `uk_transaction_request_id`;


-- (request_id, type) 조합으로 유니크 인덱스 생성함
ALTER TABLE `transaction`
  ADD UNIQUE KEY `uk_tx_reqid_type` (`request_id`,`type`);