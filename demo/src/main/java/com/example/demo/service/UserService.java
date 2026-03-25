package com.example.demo.service;

import com.example.demo.dto.UserRegistrationDto;
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
import com.example.demo.entity.User;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserRegistrationDto request) {
        if (userRepository.findByEmailId(request.getEmailId()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmailId(request.getEmailId());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setBio(request.getBio());
        user.setRole(Role.ROLE_USER);

        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        if (!target.isProfilePublic()) {
            User viewer = getCurrentUserOrThrow();
            boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
            if (!isAdmin && !viewer.getId().equals(userId)) {
                throw new AccessDeniedException("This profile is private");
            }
        }
        return target;
    }

    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User viewer = getCurrentUserOrThrow();
        boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
        if (isAdmin) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findVisibleToViewer(viewer.getId(), pageable);
    }

    public User updateUser(Long userId, UserRegistrationDto request) {
        assertSelfOrAdmin(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setUserName(request.getUserName());
        user.setEmailId(request.getEmailId());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        user.setBio(request.getBio());
        if (request.getIsProfilePublic() != null) {
            user.setProfilePublic(request.getIsProfilePublic());
        }

        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        assertSelfOrAdmin(userId);

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public List<User> searchUsersByName(String userName) {
        User viewer = getCurrentUserOrThrow();
        boolean isAdmin = viewer.getRole() == Role.ROLE_ADMIN;
        return userRepository.findByUserNameContainingIgnoreCase(userName)
                .stream()
                .filter(u -> u.isProfilePublic() || isAdmin || u.getId().equals(viewer.getId()))
                .collect(Collectors.toList());
    }

    public User getCurrentUser() {
        return getCurrentUserOrThrow();
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
