CREATE TABLE authors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  author_name VARCHAR(255) NOT NULL,
   bio TEXT,
  profile_picture_url VARCHAR(255)
);