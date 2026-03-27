package com.example.demo.service;

import com.example.demo.dto.response.LikeResponseDto;
import com.example.demo.dto.response.LikeStatsResponseDto;
import com.example.demo.entity.ActivityType;
import com.example.demo.entity.ListLike;
import com.example.demo.entity.Lists;
import com.example.demo.entity.User;
import com.example.demo.repository.ListLikeRepository;
import com.example.demo.repository.ListsRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
public class ListLikeService {

    private final ListLikeRepository listLikeRepository;
    private final UserRepository userRepository;
    private final ListsRepository listsRepository;
    private final ActivityService activityService;

    @Transactional
    public LikeResponseDto likeList(Long listId) {
        Long currentUserId = getCurrentUserIdOrThrow();
        
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + currentUserId));
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));

        if (listLikeRepository.existsByUserIdAndListId(currentUserId, listId)) {
            throw new RuntimeException("User has already liked this list");
        }

        ListLike like = new ListLike();
        like.setUser(user);
        like.setList(list);

        try {
            ListLike saved = listLikeRepository.save(like);
            activityService.record(user, ActivityType.LIST_LIKED, list.getId(), Map.of(
                    "listTitle", list.getTitle()
            ));
            return mapToDto(saved);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("User has already liked this list");
        }
    }

    @Transactional
    public void unlikeList(Long listId) {
        Long currentUserId = getCurrentUserIdOrThrow();
        
        ListLike like = listLikeRepository.findByUserIdAndListId(currentUserId, listId)
                .orElseThrow(() -> new RuntimeException("Like not found"));
        listLikeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public Page<LikeResponseDto> getListLikes(Long listId, int page, int size) {
        if (!listsRepository.existsById(listId)) {
            throw new RuntimeException("List not found with ID: " + listId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return listLikeRepository.findByListId(listId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public LikeStatsResponseDto getListLikeStats(Long listId) {
        if (!listsRepository.existsById(listId)) {
            throw new RuntimeException("List not found with ID: " + listId);
        }
        
        Long likeCount = listLikeRepository.countByListId(listId);
        
        Long currentUserId = getCurrentUserIdAttempt();
        Boolean likedByCurrentUser = false;
        if (currentUserId != null) {
            likedByCurrentUser = listLikeRepository.existsByUserIdAndListId(currentUserId, listId);
        }
        
        return new LikeStatsResponseDto(likeCount, likedByCurrentUser);
    }

    @Transactional(readOnly = true)
    public boolean hasUserLikedList(Long userId, Long listId) {
        return listLikeRepository.existsByUserIdAndListId(userId, listId);
    }

    private LikeResponseDto mapToDto(ListLike like) {
        LikeResponseDto dto = new LikeResponseDto();
        dto.setId(like.getId());
        dto.setUserId(like.getUser().getId());
        dto.setUserName(like.getUser().getUserName());
        dto.setTargetId(like.getList().getId());
        dto.setCreatedAt(like.getCreatedAt());
        return dto;
    }

    private Long getCurrentUserIdOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("User not authenticated");
        }
        String principal = authentication.getName();
        User user = userRepository.findByEmailId(principal)
            .or(() -> userRepository.findByUserName(principal))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }

    private Long getCurrentUserIdAttempt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        String principal = authentication.getName();
        return userRepository.findByEmailId(principal)
            .or(() -> userRepository.findByUserName(principal))
            .map(User::getId)
            .orElse(null);
    }
}
