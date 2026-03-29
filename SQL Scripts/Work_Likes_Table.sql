CREATE TABLE IF NOT EXISTS work_likes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  work_id BIGINT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  UNIQUE KEY uk_work_like (user_id, work_id)
);

CREATE INDEX idx_work_likes_work_id ON work_likes(work_id);
