CREATE TABLE book_genre(
book_id BIGINT NOT NULL,
genre_id BIGINT NOT NULL,

PRIMARY KEY (book_id, genre_id),
FOREIGN KEY (book_id) REFERENCES books(id),
FOREIGN KEY (genre_id) REFERENCES genre(id)
)