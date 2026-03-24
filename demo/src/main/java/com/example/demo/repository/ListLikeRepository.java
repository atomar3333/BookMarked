package com.example.demo.repository;

import com.example.demo.entity.ListLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListLikeRepository extends JpaRepository<ListLike, Long> {
    Page<ListLike> findByListId(Long listId, Pageable pageable);

    Optional<ListLike> findByUserIdAndListId(Long userId, Long listId);

    Long countByListId(Long listId);

    boolean existsByUserIdAndListId(Long userId, Long listId);
}
