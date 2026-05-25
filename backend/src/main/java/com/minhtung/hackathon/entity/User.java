package com.minhtung.hackathon.entity;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
//bang user xac nhan
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @Column(name = "UserName",nullable = false , unique = true)

    private String username ;
    @Column(name = "Password",nullable = false)

    private String password ;
    @Column(name = "Email",nullable = false)
    private String email ;
    @Column(name = "Active",nullable = false)
    private boolean active =false ;

    @Column
    private String token ;

    @Column
    private LocalDateTime expiredAt;


    public boolean isExpired() {
        if (expiredAt == null) return true;
        return LocalDateTime.now().isAfter(expiredAt);
    }
    public User() {
    }

    public User(String username, String password, String email, boolean active, String token, LocalDateTime expiredAt) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.active = active;
        this.token = token;
        this.expiredAt = expiredAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}
