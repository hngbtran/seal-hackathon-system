package com.minhtung.hackathon.entity;


import com.minhtung.hackathon.enums.TeamStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;


    @Column(name = "Name",nullable = false , length = 100)
    private String name ;

    @Column(name = "Status", length = 20 , nullable = false)
    private TeamStatus status = TeamStatus.OPEN;
    @Column(name = "CreateAt" , nullable = false )
    private LocalDate createAt = LocalDate.now() ;

    @ManyToOne
    @JoinColumn(name = "LeaderId" )
    private User leader ;
   @Column(name = "InviteCode",nullable = false,unique = true)

    private String inviteCode ;
   @Column(name = "TrackInt")
    private Integer trackint ;
   @Column(name = "column")
    private Integer colum ;
    @Column(name = "CompetitionStatus")
    private Integer competitionStatus ;



}
