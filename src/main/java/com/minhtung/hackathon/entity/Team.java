package com.minhtung.hackathon.entity;


import com.minhtung.hackathon.enums.TeamStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;


    @Column(name = "Name",nullable = false , length = 100)
    private String name ;

    @Column(name = "Status", length = 20 , nullable = false)
    private TeamStatus status = TeamStatus.OPEN;
    @Column(name = "CreateAt" , nullable = false , unique = true)
    private LocalDate createAt = LocalDate.now() ;
    @Column(name = "LeaderId",length =  10 , unique = true)
    private int leaderID ;
   @Column(name = "InviteCode",nullable = false,unique = true)

    private String inviteCode ;
   @Column(name = "TrackInt")
    private Integer trackint ;
   @Column(name = "colum")
    private Integer colum ;
    @Column(name = "CompetitionStatus")
    private Integer competitionStatus ;


    public Team() {
    }

    public Team(String name, int leaderID, String inviteCode) {
        this.name = name;
        this.leaderID = leaderID;
        this.inviteCode = inviteCode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TeamStatus getStatus() {
        return status;
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
    }

    public LocalDate getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDate createAt) {
        this.createAt = createAt;
    }

    public int getLeaderID() {
        return leaderID;
    }

    public void setLeaderID(int leaderID) {
        this.leaderID = leaderID;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public Integer getTrackint() {
        return trackint;
    }

    public void setTrackint(Integer trackint) {
        this.trackint = trackint;
    }

    public Integer getColum() {
        return colum;
    }

    public void setColum(Integer colum) {
        this.colum = colum;
    }

    public Integer getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(Integer competitionStatus) {
        this.competitionStatus = competitionStatus;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", createAt=" + createAt +
                ", leaderID=" + leaderID +
                ", inviteCode='" + inviteCode + '\'' +
                ", trackint=" + trackint +
                ", colum=" + colum +
                ", competitionStatus=" + competitionStatus +
                '}';
    }
}
