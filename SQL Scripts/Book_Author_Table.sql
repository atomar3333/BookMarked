CREATE TABLE book_author(
book_id BIGINT NOT NULL,
author_id BIGINT NOT NULL,

PRIMARY KEY (book_id, author_id),
FOREIGN KEY (book_id) REFERENCES books(id),
FOREIGN KEY (author_id) REFERENCES authors(id)
)