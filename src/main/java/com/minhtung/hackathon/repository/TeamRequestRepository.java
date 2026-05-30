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

     //lấy lời mời đang chờ của 1 user (gửi mã xác nhận qua gmail)

     List<TeamRequest> findByReceiverIdAndTypeAndStatus(Long receiver , long id ,boolean status);
     //lay join request dang cho của 1 dôi (leader_xem )
     List<TeamRequest> findByTeamIdAndTypeAndStatus(Long teamId , RequestType type  , RequestStatus status) ;


     //kiem tra da gui request loại nay chưa tránh spam
     boolean exitsBySenderAndTeamIdAndTypeAndStatus(int senderId , Long teamId , RequestType type ,  RequestStatus status);


     Optional<TeamRequest> findByTeamIdAndTypeAndStatus(Long teamId , RequestType type , boolean status);







}
