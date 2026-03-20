CREATE TABLE books (
  id BIGINT PRIMARY KEY auto_increment,
  google_books_id VARCHAR(255) UNIQUE,
  title VARCHAR(255) UNIQUE NOT NULL,
  author VARCHAR(50) NOT NULL,
  description TEXT,
  published_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  isbn VARCHAR(20) UNIQUE,
  cover_image_url VARCHAR(255)
);