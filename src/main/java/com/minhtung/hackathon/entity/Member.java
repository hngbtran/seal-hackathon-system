package com.minhtung.hackathon.entity;

import com.minhtung.hackathon.enums.MemberRole;
import jakarta.persistence.*;

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
     @Column(name = "TeamId" , nullable = false)
    private long teamId ;
      @Column(name = "MemberId",nullable = false)
    private long memberID ;

    public Member() {
    }

    public Member( MemberRole role, boolean status, long teamId, long memberID) {

        this.role = role;
        this.status = status;
        this.teamId = teamId;
        this.memberID = memberID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public long getMemberID() {
        return memberID;
    }

    public void setMemberID(int memberID) {
        this.memberID = memberID;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", role=" + role +
                ", status=" + status +
                ", teamId=" + teamId +
                ", memberID=" + memberID +
                '}';
    }
}
