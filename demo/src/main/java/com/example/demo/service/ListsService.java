package com.example.demo.service;

import com.example.demo.dto.CreateListDto;
import com.example.demo.dto.ListDto;
import com.example.demo.dto.UpdateListDto;
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

@Service
@RequiredArgsConstructor
public class ListsService {

    private final ListsRepository listsRepository;
    private final UserRepository userRepository;

    public ListDto createList(CreateListDto request) {
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

        return mapToDto(listsRepository.save(list));
    }

    public ListDto getListById(Long listId) {
        return mapToDto(listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId)));
    }

    public Page<ListDto> getAllLists(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return listsRepository.findAll(pageable).map(this::mapToDto);
    }

    public Page<ListDto> getListsByUser(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        Pageable pageable = PageRequest.of(page, size);
        return listsRepository.findByUserId(userId, pageable).map(this::mapToDto);
    }

    public ListDto updateList(Long listId, UpdateListDto request) {
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

        return mapToDto(listsRepository.save(list));
    }

    public void deleteList(Long listId) {
        Lists list = listsRepository.findById(listId)
                .orElseThrow(() -> new RuntimeException("List not found with ID: " + listId));

        assertListOwnerOrAdmin(list);
        listsRepository.deleteById(listId);
    }

    private ListDto mapToDto(Lists list) {
        ListDto dto = new ListDto();
        dto.setId(list.getId());
        dto.setUserId(list.getUser().getId());
        dto.setTitle(list.getTitle());
        dto.setDescription(list.getDescription());
        dto.setCreatedDate(list.getCreatedDate());
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

    private void assertListOwnerOrAdmin(Lists list) {
        User currentUser = getCurrentUserOrThrow();
        boolean isAdmin = currentUser.getRole() == Role.ROLE_ADMIN;
        if (!isAdmin && !currentUser.getId().equals(list.getUser().getId())) {
            throw new AccessDeniedException("You can only modify your own lists");
        }
    }
}
