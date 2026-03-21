package com.example.demo.entity;

import com.example.demo.entity.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name="username",unique = true,nullable = false)
    private String userName;

    @Column(name="email",unique = true,nullable = false)
    private String emailId;
    //
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "profile_picture_url", length = 255)
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.ROLE_USER;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Review> reviews;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<ReadingStatus> readingStatuses;
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
//    private List<Lists> lists;
//
//    @OneToMany(mappedBy = "followed_User", cascade = CascadeType.ALL)
//    private List<Follower> following;
//
//    @OneToMany(mappedBy = "follower_User", cascade = CascadeType.ALL)
//    private List<Follower> followers;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

//    public List<Review> getReviews() {
//        return reviews;
//    }
//
//    public void setReviews(List<Review> reviews) {
//        this.reviews = reviews;
//    }
//
//    public List<ReadingStatus> getReadingStatuses() {
//        return readingStatuses;
//    }
//
//    public void setReadingStatuses(List<ReadingStatus> readingStatuses) {
//        this.readingStatuses = readingStatuses;
//    }
//
//    public List<Lists> getLists() {
//        return lists;
//    }
//
//    public void setLists(List<Lists> lists) {
//        this.lists = lists;
//    }
//
//    public List<Follower> getFollowing() {
//        return following;
//    }
//
//    public void setFollowing(List<Follower> following) {
//        this.following = following;
//    }
//
//    public List<Follower> getFollowers() {
//        return followers;
//    }
//
//    public void setFollowers(List<Follower> followers) {
//        this.followers = followers;
//    }
}
