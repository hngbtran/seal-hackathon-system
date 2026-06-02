package com.minhtung.hackathon.entity;


import com.minhtung.hackathon.enums.RequestStatus;
import com.minhtung.hackathon.enums.RequestType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TeamRequest")
public class TeamRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20, nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "SenderId", nullable = false)
    private  User sender;
    @ManyToOne
    @JoinColumn (name = "ReceiverID", nullable = false)
    private User receiver;
    @ManyToOne
    @JoinColumn (name = "TeamId", nullable = false)
    private Team team;
    //JOIN_REQUEST , INVATION ,  LEAVE_REQUEST, TEAM_SUBMISSION
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", length = 255, nullable = false)
    private RequestType type;






}
