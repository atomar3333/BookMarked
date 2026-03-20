package com.example.demo.repository;

import com.example.demo.entity.BookList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookListRepository extends JpaRepository<BookList, Long> {
    Page<BookList> findByListId(Long listId, Pageable pageable);
    Optional<BookList> findByListIdAndBookId(Long listId, Long bookId);
    long countByListId(Long listId);
}
