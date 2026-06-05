package com.minhtung.hackathon.entity;


import com.minhtung.hackathon.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
//bang user xac nhan
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    @Column(name = "UserName",nullable = false , unique = true)


    private String fullName ;
    @Column(name = "Password",nullable = true)

    private String password ;
    @Column(name = "Email",nullable = true)
    private String email ;
    @Column(name = "Active",nullable = true)
    private boolean status  =false ;

    @Column
    private String token ; //verify code


    @Column(name = "Student" , nullable = true)
    private String StudendId ;

    @Column(name = "SchoolName" , nullable = false)

    private String schoolName ;




    @Column
    private LocalDateTime expiredAt;



    //Role moi them User/Lecturer/Admin
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER ;
    @OneToOne(mappedBy = "leader")
    private Team leadTeam;

    @OneToMany(mappedBy = "sender")
    private List<TeamRequest> sentRequests;

    @OneToMany(mappedBy = "receiver")
    private List<TeamRequest> receivedRequests;

    public boolean isExpired() {
        if (expiredAt == null) return true;
        return LocalDateTime.now().isAfter(expiredAt);
    }

}
