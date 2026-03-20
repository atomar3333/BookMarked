package com.example.demo.repository;

import com.example.demo.entity.Follower;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowerRepository extends JpaRepository<Follower, Long> {
    // People who follow userId (following.id = userId)
    Page<Follower> findByFollowingId(Long followingId, Pageable pageable);

    // People userId is following (follower.id = userId)
    Page<Follower> findByFollowerId(Long followerId, Pageable pageable);

    Optional<Follower> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowingId(Long followingId);
    long countByFollowerId(Long followerId);
}
