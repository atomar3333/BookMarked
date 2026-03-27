CREATE TABLE works (
  id BIGINT PRIMARY KEY auto_increment,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  open_library_work_id VARCHAR(30) UNIQUE,
  first_publish_year INT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
