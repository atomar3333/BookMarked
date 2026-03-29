CREATE TABLE editions (
  id BIGINT PRIMARY KEY auto_increment,
  open_library_work_id VARCHAR(30),
  open_library_edition_id VARCHAR(30) UNIQUE,
  title VARCHAR(255),
  notes TEXT,
  covers TEXT,
  identifiers TEXT,
  latest_revision INT,
  revision INT,
  created TEXT,
  last_modified DATETIME,
  isbn10 VARCHAR(10) UNIQUE,
  isbn13 VARCHAR(13) UNIQUE,
  publishers TEXT,
  publish_date TEXT,
  page_count INT,
  binding VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  -- foreign key and index on work_id removed
  INDEX idx_editions_publishers (publishers)
);
