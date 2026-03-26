package com.example.demo.service;

import com.example.demo.dto.ActivityDto;
import com.example.demo.entity.Activity;
import com.example.demo.entity.ActivityType;
import com.example.demo.entity.Follower;
import com.example.demo.entity.Lists;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.ActivityRepository;
import com.example.demo.repository.FollowerRepository;
import com.example.demo.repository.ListsRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final FollowerRepository followerRepository;
    private final ListsRepository listsRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(User user, ActivityType activityType, Long targetId, Map<String, Object> metadata) {
        Activity activity = new Activity();
        activity.setUser(user);
        activity.setActivityType(activityType);
        activity.setTargetId(targetId);
        activity.setMetadata(writeMetadata(metadata));
        activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public Page<ActivityDto> getMyActivities(int page, int size, ActivityType type) {
        User viewer = getCurrentUserOrThrow();
        List<Activity> activities = type == null
                ? activityRepository.findByUserIdOrderByCreatedAtDesc(viewer.getId())
                : activityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(viewer.getId(), type);

        return paginate(activities.stream().map(this::mapToDto).collect(Collectors.toList()), page, size);
    }

    @Transactional(readOnly = true)
    public Page<ActivityDto> getUserActivities(Long userId, int page, int size, ActivityType type) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        User viewer = getCurrentUserOrThrow();

        boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = viewer.getId().equals(userId);
        if (!target.isProfilePublic() && !isAdmin && !isSelf) {
            throw new AccessDeniedException("This profile is private");
        }

        List<Activity> activities = type == null
                ? activityRepository.findByUserIdOrderByCreatedAtDesc(userId)
                : activityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(userId, type);

        List<ActivityDto> visible = activities.stream()
                .filter(activity -> canViewActivity(activity, viewer))
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return paginate(visible, page, size);
    }

    @Transactional(readOnly = true)
    public Page<ActivityDto> getFeed(int page, int size, ActivityType type) {
        User viewer = getCurrentUserOrThrow();
        List<Long> followedUserIds = followerRepository.findByFollowerId(viewer.getId(), Pageable.unpaged())
                .stream()
                .map(Follower::getFollowing)
                .filter(Objects::nonNull)
                .map(User::getId)
                .collect(Collectors.toList());

        if (followedUserIds.isEmpty()) {
            return Page.empty(PageRequest.of(page, size));
        }

        List<Activity> activities = type == null
                ? activityRepository.findByUserIdInOrderByCreatedAtDesc(followedUserIds)
                : activityRepository.findByUserIdInAndActivityTypeOrderByCreatedAtDesc(followedUserIds, type);

        List<ActivityDto> visible = activities.stream()
                .filter(activity -> canViewActivity(activity, viewer))
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return paginate(visible, page, size);
    }

    private boolean canViewActivity(Activity activity, User viewer) {
        User actor = activity.getUser();
        boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = viewer.getId().equals(actor.getId());

        if (!actor.isProfilePublic() && !isAdmin && !isSelf) {
            return false;
        }

        if (activity.getActivityType() == ActivityType.LIST_CREATED || activity.getActivityType() == ActivityType.LIST_LIKED) {
            return canViewListBackedActivity(activity, viewer, isAdmin);
        }

        return true;
    }

    private boolean canViewListBackedActivity(Activity activity, User viewer, boolean isAdmin) {
        Optional<Lists> list = listsRepository.findById(activity.getTargetId());
        if (list.isEmpty()) {
            return false;
        }

        Lists found = list.get();
        return found.isPublic() || isAdmin || viewer.getId().equals(found.getUser().getId());
    }

    private ActivityDto mapToDto(Activity activity) {
        ActivityDto dto = new ActivityDto();
        dto.setId(activity.getId());
        dto.setUserId(activity.getUser().getId());
        dto.setUserName(activity.getUser().getUserName());
        dto.setActivityType(activity.getActivityType());
        dto.setTargetId(activity.getTargetId());
        dto.setMetadata(readMetadata(activity.getMetadata()));
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }

    private Page<ActivityDto> paginate(List<ActivityDto> activities, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        if (start >= activities.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, activities.size());
        }

        int end = Math.min(start + pageable.getPageSize(), activities.size());
        return new PageImpl<>(activities.subList(start, end), pageable, activities.size());
    }

    private String writeMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Unable to serialize activity metadata", e);
        }
    }

    private JsonNode readMetadata(String metadata) {
        if (metadata == null || metadata.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse activity metadata", e);
        }
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByEmailId(authentication.getName())
                .or(() -> userRepository.findByUserName(authentication.getName()))
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }
}
