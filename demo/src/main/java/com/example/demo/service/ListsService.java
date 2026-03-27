package com.example.demo.service;

import com.example.demo.dto.request.CreateListRequestDto;
import com.example.demo.dto.request.UpdateListRequestDto;
import com.example.demo.dto.response.ListResponseDto;
import com.example.demo.entity.ActivityType;
import com.example.demo.entity.Lists;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.ListsRepository;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListsService {

    private final ListsRepository listsRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    @Transactional
    public ListResponseDto createList(CreateListRequestDto request) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(request.getUserId())) {
            throw new AccessDeniedException("You can only create lists for your own account");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new RuntimeException("List title cannot be empty");
        }

        if (request.getTitle().length() > 255) {
            throw new RuntimeException("List title cannot exceed 255 characters");
        }

        Lists list = new Lists();
        list.setUser(user);
        list.setTitle(request.getTitle());
        list.setDescription(request.getDescription());
        list.setPublic(request.isPublic());

        Lists saved = listsRepository.save(list);
        activityService.record(user, ActivityType.LIST_CREATED, saved.getId(), Map.of(
            "listTitle", saved.getTitle()
        ));

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public ListResponseDto getListById(Long listId) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));
        assertListVisibleToCurrentViewer(list);
        return mapToDto(list);
    }

    @Transactional(readOnly = true)
    public Page<ListResponseDto> getAllLists(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User viewer = getCurrentUserOrThrow();
        if (viewer.getRole() == Role.ROLE_ADMIN) {
            return listsRepository.findAll(pageable).map(this::mapToDto);
        }
        return listsRepository.findVisibleToViewer(viewer.getId(), pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ListResponseDto> getListsByUser(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size);
        User viewer = getCurrentUserOrThrow();
        boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
        boolean isSelf = viewer.getId().equals(userId);
        if (isAdmin || isSelf) {
            return listsRepository.findByUserId(userId, pageable).map(this::mapToDto);
        }
        return listsRepository.findByUserIdAndIsPublicTrue(userId, pageable).map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<ListResponseDto> searchLists(String query) {
        if (query == null || query.isBlank()) {
            throw new RuntimeException("Search query cannot be empty");
        }

        User viewer = getCurrentUserOrThrow();
        List<Lists> visibleLists;
        if (viewer.getRole() == Role.ROLE_ADMIN) {
            visibleLists = listsRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
        } else {
            visibleLists = listsRepository.findVisibleToViewerByTitleOrDescriptionContaining(viewer.getId(), query);
        }

        return visibleLists
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ListResponseDto updateList(Long listId, UpdateListRequestDto request) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));

        assertListOwnerOrAdmin(list);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            if (request.getTitle().length() > 255) {
                throw new RuntimeException("List title cannot exceed 255 characters");
            }
            list.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            list.setDescription(request.getDescription());
        }

        if (request.getIsPublic() != null) {
            list.setPublic(request.getIsPublic());
        }

        return mapToDto(listsRepository.save(list));
    }

    @Transactional
    public void deleteList(Long listId) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));

        assertListOwnerOrAdmin(list);
        listsRepository.deleteById(listId);
    }

    private ListResponseDto mapToDto(Lists list) {
        ListResponseDto dto = new ListResponseDto();
        dto.setId(list.getId());
        dto.setUserId(list.getUser().getId());
        dto.setTitle(list.getTitle());
        dto.setDescription(list.getDescription());
        dto.setCreatedDate(list.getCreatedDate());
        dto.setPublic(list.isPublic());
        return dto;
    }

    public void assertListVisibility(Long listId) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));
        assertListVisibleToCurrentViewer(list);
    }

    private void assertListVisibleToCurrentViewer(Lists list) {
        if (!list.isPublic()) {
            User viewer = getCurrentUserOrThrow();
            boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
            if (!isAdmin && !viewer.getId().equals(list.getUser().getId())) {
                throw new AccessDeniedException("This list is private");
            }
        }
    }

    private User getCurrentUserOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AccessDeniedException("Authentication required");
        }

        return userRepository.findByEmailId(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found"));
    }

    private void assertListOwnerOrAdmin(Lists list) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(list.getUser().getId())) {
            throw new AccessDeniedException("You can only modify your own lists");
        }
    }
}
