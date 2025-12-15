package com.kma.backend.dto;

public class UserDTO {
    private Long id;
    private String username;
    private String role;
    private String fullname;
    private String email;
    private String phone;
    private Boolean isActive;

    public UserDTO() {}

    public UserDTO(Long id, String username, String role, String fullname, String email, String phone, Boolean isActive) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
