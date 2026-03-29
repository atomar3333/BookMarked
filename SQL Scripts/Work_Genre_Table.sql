CREATE TABLE IF NOT EXISTS work_genre (
  work_id BIGINT NOT NULL,
  genre_id BIGINT NOT NULL,
  PRIMARY KEY (work_id, genre_id),
  FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  FOREIGN KEY (genre_id) REFERENCES genre(id) ON DELETE CASCADE
);

CREATE INDEX idx_work_genre_genre_id ON work_genre(genre_id);
