package com.example.demo.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserProfileResponseDto {

    private Long id;
    private String userName;
    private String emailId;
    private String bio;
    private boolean isProfilePublic;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @JsonProperty("isProfilePublic")
    public boolean isProfilePublic() {
        return isProfilePublic;
    }

    public void setProfilePublic(boolean profilePublic) {
        isProfilePublic = profilePublic;
    }
}
