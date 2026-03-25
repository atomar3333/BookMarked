CREATE TABLE reviews (
  id BIGINT PRIMARY KEY auto_increment,
  user_id BIGINT NOT NULL,
  book_id BIGINT NOT NULL,
  review_text text,
  rating DECIMAL(3,2) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
  UNIQUE KEY (user_id, book_id),
  CONSTRAINT chk_rating CHECK (rating >= 0.00 AND rating <= 5.00)

);