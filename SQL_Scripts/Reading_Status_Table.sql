CREATE TABLE reading_status (
  id BIGINT PRIMARY KEY auto_increment,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  current_status ENUM('WANT_TO_READ', 'CURRENTLY_READING', 'READ'),
  started_at DATE,
  finished_at DATE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
  UNIQUE KEY (user_id, book_id)
);