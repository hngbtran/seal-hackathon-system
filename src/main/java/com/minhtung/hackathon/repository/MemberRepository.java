package com.minhtung.hackathon.repository;

import com.minhtung.hackathon.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    List<Member> findByTeamIdAndStatus(Integer teamId, boolean status);


    Optional<Member> findByTeamIdAndMemberID(Long teamId , Long MemberID);

    boolean existsByTeamIdAndMemberIDAndStatus(Long teamId , Long MemberId , boolean status) ;


    //đếm số lượng thành viên trong team trạng thái active
    int countByTeamIdAndStatus(long teamId , boolean status);

    boolean existsByMemberIDAndStatus(long memberID , boolean status);




}
