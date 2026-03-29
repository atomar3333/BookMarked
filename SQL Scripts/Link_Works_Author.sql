-- For MySQL 5.7+ with JSON support

INSERT IGNORE INTO work_author (work_id, author_id)
SELECT
    w.id AS work_id,
    a.id AS author_id
FROM
    works w
    JOIN authors a
    ON JSON_CONTAINS(w.authors, JSON_QUOTE(a.ol_author_id), '$');