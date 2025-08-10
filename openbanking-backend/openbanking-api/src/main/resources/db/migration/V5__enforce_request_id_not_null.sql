UPDATE `transaction`
SET request_id = CONCAT('LEGACY-', id)
WHERE request_id IS NULL OR request_id = '';

ALTER TABLE `transaction`
  MODIFY request_id VARCHAR(64) NOT NULL;