CREATE TABLE IF NOT EXISTS work_author (
  work_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  PRIMARY KEY (work_id, author_id),
  FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  FOREIGN KEY (author_id) REFERENCES authors(id) ON DELETE CASCADE
);

CREATE INDEX idx_work_author_author_id ON work_author(author_id);
