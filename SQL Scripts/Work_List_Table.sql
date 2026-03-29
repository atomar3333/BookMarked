CREATE TABLE IF NOT EXISTS work_list (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  list_id BIGINT NOT NULL,
  work_id BIGINT NOT NULL,
  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE,
  FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  UNIQUE KEY uk_work_list (list_id, work_id)
);

CREATE INDEX idx_work_list_work_id ON work_list(work_id);
