package com.example.demo.repository;

import com.example.demo.entity.BookAuthor;
import com.example.demo.entity.BookAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookAuthorRepository extends JpaRepository<BookAuthor, BookAuthorId> {
    List<BookAuthor> findByBookIdAndAuthorId(Long bookId, Long authorId);
    List<BookAuthor> findByBookId(Long bookId);
    List<BookAuthor> findByAuthorId(Long authorId);
}
