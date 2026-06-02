package com.minhtung.hackathon.entity;

import com.minhtung.hackathon.enums.MemberRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {


     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;



    @Enumerated(EnumType.STRING)
    @Column(name = "Role " , nullable = false)
    private MemberRole role ;



    //true la dang o trong doi , false da roi di
    @Column(name = "Status" , nullable = false)
    private boolean status ;
    @ManyToOne
     @JoinColumn(name = "TeamId" , nullable = false)
    private Team team ;

    @ManyToOne

      @JoinColumn(name = "MemberId",nullable = false)
    private User member ;


}
