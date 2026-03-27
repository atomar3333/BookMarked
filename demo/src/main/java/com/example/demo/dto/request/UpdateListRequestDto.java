package com.example.demo.dto.request;

import jakarta.validation.constraints.Size;

public class UpdateListRequestDto {

    @Size(max = 255, message = "title cannot exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "description cannot exceed 5000 characters")
    private String description;

    private Boolean isPublic;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
}
