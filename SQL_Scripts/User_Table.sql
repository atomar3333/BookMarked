CREATE TABLE users (
   id BIGINT PRIMARY KEY auto_increment,
   username VARCHAR(50) UNIQUE NOT NULL,
   email VARCHAR(50) UNIQUE NOT NULL,
   password_hash VARCHAR(255) UNIQUE NOT NULL,
   bio TEXT,
   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   profile_picture_url VARCHAR(255)
 );
