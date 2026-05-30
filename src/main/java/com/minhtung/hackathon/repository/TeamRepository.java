package com.minhtung.hackathon.repository;

import com.minhtung.hackathon.entity.Team;
import com.minhtung.hackathon.enums.TeamStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
    Optional<Team> findByInviteCode(String inviteCode) ;

    List<Team>findByNameContainingIgnoreCaseAndStatus(String name , TeamStatus status) ;
    boolean exitsByInviteCode(String inviteCode);

    List<Team> findbyStatus(TeamStatus status) ;


}
