CREATE TABLE followers (
  id BIGINT PRIMARY KEY auto_increment,
  following_id BIGINT NOT NULL,
  follower_id BIGINT NOT NULL,
  FOREIGN KEY(following_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY(follower_id) REFERENCES users(id) ON DELETE CASCADE,
  UNIQUE KEY (following_id, follower_id)
);