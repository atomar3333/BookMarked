package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateAuthorRequestDto {
    @Size(min = 1, max = 255, message = "Author name must be between 1 and 255 characters")
    private String authorName;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    private String profilePictureUrl;

    public UpdateAuthorRequestDto() {}

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}
