INSERT INTO work_edition (work_id, edition_id)
SELECT w.id AS work_id, e.id AS edition_id
FROM editions e
JOIN works w ON REPLACE(w.open_library_work_id, '/works/', '') = e.open_library_work_id
WHERE NOT EXISTS (
  SELECT 1 FROM work_edition we
  WHERE we.work_id = w.id AND we.edition_id = e.id
);