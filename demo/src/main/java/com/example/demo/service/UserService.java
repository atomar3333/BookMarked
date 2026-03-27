package com.example.demo.service;

import com.example.demo.dto.request.CreateUserRequestDto;
import com.example.demo.dto.request.RegisterRequestDto;
import com.example.demo.dto.request.UpdateUserProfileRequestDto;
import com.example.demo.dto.response.UserProfileResponseDto;
import com.example.demo.entity.Role;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.User;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createUser(RegisterRequestDto request) {
        createUserInternal(
                request.getUserName(),
                request.getEmailId(),
                request.getPassword(),
                request.getBio(),
                request.getIsProfilePublic()
        );
    }

    @Transactional
    public UserProfileResponseDto createUser(CreateUserRequestDto request) {
        User saved = createUserInternal(
                request.getUserName(),
                request.getEmailId(),
                request.getPassword(),
                request.getBio(),
                request.getIsProfilePublic()
        );
        return mapToUserProfileResponse(saved);
    }

    private User createUserInternal(
            String userName,
            String emailId,
            String rawPassword,
            String bio,
            Boolean isProfilePublic
    ) {
        if (userRepository.findByEmailId(emailId).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUserName(userName);
        user.setEmailId(emailId);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setBio(bio);
        user.setProfilePublic(isProfilePublic == null || isProfilePublic);
        user.setRole(Role.ROLE_USER);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getUserById(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        if (!target.isProfilePublic()) {
            User viewer = getCurrentUserOrThrow();
            boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
            if (!isAdmin && !viewer.getId().equals(userId)) {
                throw new AccessDeniedException("This profile is private");
            }
        }
        return mapToUserProfileResponse(target);
    }

    @Transactional(readOnly = true)
    public Page<UserProfileResponseDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User viewer = getCurrentUserOrThrow();
        boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
        if (isAdmin) {
            return userRepository.findAll(pageable).map(this::mapToUserProfileResponse);
        }
        return userRepository.findVisibleToViewer(viewer.getId(), pageable).map(this::mapToUserProfileResponse);
    }

    @Transactional
    public UserProfileResponseDto updateUser(Long userId, UpdateUserProfileRequestDto request) {
        assertSelfOrAdmin(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (request.getUserName() != null && !request.getUserName().isBlank()) {
            user.setUserName(request.getUserName());
        }
        if (request.getEmailId() != null && !request.getEmailId().isBlank()) {
            user.setEmailId(request.getEmailId());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getIsProfilePublic() != null) {
            user.setProfilePublic(request.getIsProfilePublic());
        }

        return mapToUserProfileResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        assertSelfOrAdmin(userId);

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponseDto> searchUsersByName(String userName) {
        User viewer = getCurrentUserOrThrow();
        boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
        return userRepository.findByUserNameContainingIgnoreCase(userName)
                .stream()
                .filter(u -> u.isProfilePublic() || isAdmin || u.getId().equals(viewer.getId()))
                .map(this::mapToUserProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponseDto getCurrentUser() {
        return mapToUserProfileResponse(getCurrentUserOrThrow());
    }

    private UserProfileResponseDto mapToUserProfileResponse(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();
        dto.setId(user.getId());
        dto.setUserName(user.getUserName());
        dto.setEmailId(user.getEmailId());
        dto.setBio(user.getBio());
        dto.setProfilePublic(user.isProfilePublic());
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
            throw new AccessDeniedException("You can only modify your own profile");
        }
    }
}
