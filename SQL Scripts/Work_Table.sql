CREATE TABLE works (
  id BIGINT PRIMARY KEY auto_increment,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  open_library_work_id VARCHAR(30),
  first_publish_year INT,
  subtitle VARCHAR(255),
  subjects TEXT,
  authors TEXT,
  covers TEXT,
  identifiers TEXT,
  latest_revision INT,
  revision INT,
  created DATETIME,
  last_modified DATETIME,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE (open_library_work_id)
);
