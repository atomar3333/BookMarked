package com.example.demo.service;

import com.example.demo.dto.FollowerDto;
import com.example.demo.entity.Follower;
import com.example.demo.entity.User;
import com.example.demo.repository.FollowerRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FollowerService {

    private final FollowerRepository followerRepository;
    private final UserRepository userRepository;

    public FollowerDto follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("A user cannot follow themselves");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + followerId));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + followingId));

        if (followerRepository.findByFollowerIdAndFollowingId(followerId, followingId).isPresent()) {
            throw new RuntimeException("Already following this user");
        }

        Follower follow = new Follower();
        follow.setFollower(follower);
        follow.setFollowing(following);

        return mapToDto(followerRepository.save(follow));
    }

    public void unfollow(Long followerId, Long followingId) {
        Follower follow = followerRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("Follow relationship not found"));
        followerRepository.delete(follow);
    }

    // Get all users who follow userId
    public Page<FollowerDto> getFollowers(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return followerRepository.findByFollowingId(userId, pageable).map(this::mapToDto);
    }

    // Get all users that userId is following
    public Page<FollowerDto> getFollowing(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return followerRepository.findByFollowerId(userId, pageable).map(this::mapToDto);
    }

    public Map<String, Object> getFollowerCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return Map.of("userId", userId, "followerCount", followerRepository.countByFollowingId(userId));
    }

    public Map<String, Object> getFollowingCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return Map.of("userId", userId, "followingCount", followerRepository.countByFollowerId(userId));
    }

    private FollowerDto mapToDto(Follower follow) {
        FollowerDto dto = new FollowerDto();
        dto.setId(follow.getId());
        dto.setFollowerId(follow.getFollower().getId());
        dto.setFollowingId(follow.getFollowing().getId());
        return dto;
    }
}
