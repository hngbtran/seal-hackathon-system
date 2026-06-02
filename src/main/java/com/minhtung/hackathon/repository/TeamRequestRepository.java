package com.minhtung.hackathon.repository;


import com.minhtung.hackathon.entity.Member;
import com.minhtung.hackathon.entity.TeamRequest;
import com.minhtung.hackathon.enums.RequestStatus;
import com.minhtung.hackathon.enums.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRequestRepository  extends JpaRepository<TeamRequest,Long> {

     List<TeamRequest> findByReceiver_IdAndTypeAndStatus(
             Long receiverId,
             RequestType type,
             RequestStatus status
     );

     List<TeamRequest> findByTeam_IdAndTypeAndStatus(
             Long teamId,
             RequestType type,
             RequestStatus status
     );

     boolean existsBySender_IdAndTeam_IdAndTypeAndStatus(
             Long senderId,
             Long teamId,
             RequestType type,
             RequestStatus status
     );
}











