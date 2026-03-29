CREATE TABLE work_edition (
  work_id BIGINT NOT NULL,
  edition_id BIGINT NOT NULL,
  PRIMARY KEY (work_id, edition_id),
  FOREIGN KEY (work_id) REFERENCES works(id) ON DELETE CASCADE,
  FOREIGN KEY (edition_id) REFERENCES editions(id) ON DELETE CASCADE
);

CREATE INDEX idx_work_edition_edition_id ON work_edition(edition_id);
