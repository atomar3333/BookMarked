CREATE TABLE authors (
	id BIGINT PRIMARY KEY AUTO_INCREMENT,
	author_name VARCHAR(255) NOT NULL,
	bio TEXT,
	profile_picture_url VARCHAR(255),
	personal_name VARCHAR(255),
	fuller_name VARCHAR(255),
	alternate_names TEXT,
	birth_date VARCHAR(32),
	death_date VARCHAR(32),
	photos TEXT,
	ol_author_id VARCHAR(64) UNIQUE,
	created DATETIME,
	last_modified DATETIME
);