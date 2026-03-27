CREATE TABLE editions (
  id BIGINT PRIMARY KEY auto_increment,
  work_id BIGINT NOT NULL,
  open_library_edition_id VARCHAR(30) UNIQUE,
  isbn10 VARCHAR(10) UNIQUE,
  isbn13 VARCHAR(13) UNIQUE,
  cover_image_url VARCHAR(255),
  publisher VARCHAR(255),
  publish_date DATE,
  page_count INT,
  binding VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_editions_work FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  INDEX idx_editions_work_id (work_id),
  INDEX idx_editions_publisher (publisher)
);
