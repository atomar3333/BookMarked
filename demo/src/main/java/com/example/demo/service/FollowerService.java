package com.example.demo.service;

import com.example.demo.dto.response.FollowerResponseDto;
import com.example.demo.entity.Follower;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.FollowerRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class FollowerService {

    private final FollowerRepository followerRepository;
    private final UserRepository userRepository;

    @Transactional
    public FollowerResponseDto follow(Long followerId, Long followingId) {
        assertSelfOrAdmin(followerId);

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

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        assertSelfOrAdmin(followerId);

        Follower follow = followerRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new RuntimeException("Follow relationship not found"));
        followerRepository.delete(follow);
    }

    // Get all users who follow userId
    @Transactional(readOnly = true)
    public Page<FollowerResponseDto> getFollowers(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return followerRepository.findByFollowingId(userId, pageable).map(this::mapToDto);
    }

    // Get all users that userId is following
    @Transactional(readOnly = true)
    public Page<FollowerResponseDto> getFollowing(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return followerRepository.findByFollowerId(userId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFollowerCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return Map.of("userId", userId, "followerCount", followerRepository.countByFollowingId(userId));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getFollowingCount(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        return Map.of("userId", userId, "followingCount", followerRepository.countByFollowerId(userId));
    }

    private FollowerResponseDto mapToDto(Follower follow) {
        FollowerResponseDto dto = new FollowerResponseDto();
        dto.setId(follow.getId());
        dto.setFollowerId(follow.getFollower().getId());
        dto.setFollowingId(follow.getFollowing().getId());
        return dto;
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByEmailId(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private void assertSelfOrAdmin(Long targetUserId) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException("You can only perform this action for your own account");
        }
    }
}
