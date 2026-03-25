package com.example.demo.repository;

import com.example.demo.entity.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ListsRepository extends JpaRepository<Lists, Long> {
    Page<Lists> findByUserId(Long userId, Pageable pageable);
    List<Lists> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    Page<Lists> findByIsPublicTrue(Pageable pageable);

    Page<Lists> findByUserIdAndIsPublicTrue(Long userId, Pageable pageable);

    @Query("SELECT l FROM Lists l WHERE l.isPublic = true AND " +
           "(LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Lists> findPublicByTitleOrDescriptionContaining(@Param("query") String query);

        @Query("SELECT l FROM Lists l WHERE l.isPublic = true OR l.user.id = :viewerId")
        Page<Lists> findVisibleToViewer(@Param("viewerId") Long viewerId, Pageable pageable);

        @Query("SELECT l FROM Lists l WHERE (l.isPublic = true OR l.user.id = :viewerId) AND " +
            "(LOWER(l.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(l.description) LIKE LOWER(CONCAT('%', :query, '%')))")
        List<Lists> findVisibleToViewerByTitleOrDescriptionContaining(
            @Param("viewerId") Long viewerId,
            @Param("query") String query
        );
}
