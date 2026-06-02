package com.minhtung.hackathon.repository;

import com.minhtung.hackathon.entity.Member;
import com.minhtung.hackathon.entity.Team;
import com.minhtung.hackathon.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    List<Member> findByTeam_IdAndStatus(Long teamId, boolean status);


    Optional<Member> findByTeam_IdAndMember_Id(Long teamId, Long memberId);


    Optional<Member> findByTeamAndMember(Team team , User user) ;

    boolean existsByTeam_IdAndMember_IdAndStatus(long teamId,long userId, boolean status);
    boolean existsByMember_IdAndStatus(Long memberId , boolean status);


    //đếm số lượng thành viên trong team trạng thái active
    int countByTeam_IdAndStatus(Long teamId, boolean status);






}
