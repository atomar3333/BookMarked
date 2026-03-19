package com.example.demo.service;

import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.demo.entity.User;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(UserRegistrationDto request) {
        // Business Rule: Check for duplicates
//        if (userRepository.findByUserName(request.getUserName()).isPresent()) {
//            throw new RuntimeException("Username already exists!");
//        }

        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmailId(request.getEmailId());
        // For now, storing as-is. Later, you'll add BCrypt hashing here.
        user.setPasswordHash(request.getPassword());
        user.setBio(request.getBio());

        return userRepository.save(user);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }
}
