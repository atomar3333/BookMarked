CREATE TABLE activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  activity_type ENUM(
    'BOOK_LIKED',
    'REVIEW_LIKED',
    'LIST_LIKED',
    'REVIEW_CREATED',
    'LIST_CREATED',
    'READING_STATUS_UPDATED'
  ) NOT NULL,
  target_id BIGINT NOT NULL,
  metadata TEXT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_activities_user
    FOREIGN KEY (user_id) REFERENCES users(id),

  INDEX idx_activities_user_created_at (user_id, created_at),
  INDEX idx_activities_type_created_at (activity_type, created_at),
  INDEX idx_activities_target (target_id)
);
