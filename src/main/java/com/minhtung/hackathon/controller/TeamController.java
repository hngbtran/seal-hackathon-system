package com.minhtung.hackathon.controller;


import com.minhtung.hackathon.dto.CreateTeamDto;
import com.minhtung.hackathon.dto.TeamResponseDto;
import com.minhtung.hackathon.dto.joinByCode;
import com.minhtung.hackathon.entity.User;
import com.minhtung.hackathon.repository.UserRepository;
import com.minhtung.hackathon.security.JwtUtil;
import com.minhtung.hackathon.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.OverridesAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
@Tag(name = "Team", description = "Lập đội, tham gia, tìm kiếm, chốt đội, Admin duyệt")
@SecurityRequirement(name = "bearerAuth")

public class TeamController {

    private final TeamService teamService ;
    private final JwtUtil jwtUtil ;
    private final UserRepository userRepository ;
    @Operation (summary = "Lập đội mới",
              description = "Tạo đội (status=OPEN) + gửi INVITATION qua email cho thành viên")
@PostMapping("/create")
    public ResponseEntity<?> createTeam(@RequestBody CreateTeamDto dto) {
        Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        try {
            TeamResponseDto result = teamService.createTeam(dto, Math.toIntExact(uid));
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





    @Operation(summary = "Tham gia đội qua mã mời",
            description = "Chỉ áp dụng khi đội OPEN; tự động vào đội không cần duyệt")
    @PostMapping("/join-by-code")
    public ResponseEntity<String> joinByCode(@RequestBody joinByCode dto) {
        Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(teamService.joinTeamByCode(dto.getInviteCode(), uid));
    }

    @Operation(summary = "Tim kiem doi OPEN theo ten ")
    @GetMapping("/search")
    public ResponseEntity<List<TeamResponseDto>>search(@RequestParam String keyword){
        return ResponseEntity.ok(teamService.searchTeam(keyword));
    }


    @Operation(summary = "Gửi yêu cầu xin vào đội (JOIN_REQUEST)",
            description = "Leader của đội sẽ nhận và duyệt")
    @PostMapping("/{teamId}/request-join")
    public ResponseEntity<String> requestJoin(@PathVariable Long teamId) {
        Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(teamService.sendJoinRequest(teamId, uid));
    }

    @Operation(summary = "Người được mời đồng ý hoặc từ chối INVITATION")
    @PutMapping("/invitation/{requestId}/respond")
    public ResponseEntity<String> respondInvitation(
            @PathVariable Long requestId,
            @RequestParam boolean accept)
             {
                 Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(teamService.respondToInvitation(requestId, accept, uid));
    }

    @Operation(summary = "Leader duyet hoac tu choi Join_request")
    @PutMapping("/Join-request/{requestId}/respond")
    public ResponseEntity<String> respondJoinRequest(
            @PathVariable Long requestId,
            @RequestParam boolean accept)
            {
        Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(teamService.respondToJoinRequest(requestId, accept, uid));

    }

    @Operation(summary = "thanh vien gui yeu cau xin roi doi ")
    @PostMapping("{teamId}/leave-request")
    public ResponseEntity<String> leaveRequest(@PathVariable Long teamId) {
        Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(teamService.requestLeave(teamId, uid));
    }

    @Operation(summary = "leader duyet hoac tu choi leave_request")
    @PutMapping("/Leave-request/{requestId}/respond")
    public ResponseEntity<String> respondLeaveRequest(
            @PathVariable long requestId,
            @RequestParam boolean accept)
             {
        Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(teamService.respondToLeaveRequest(requestId, accept, uid));
    }


    @Operation(summary = "Leader chốt đội gửi Admin phê duyệt (TEAM_SUBMISSION)",
            description = "Team status chuyển sang PENDING_APPROVAL")
    @PostMapping("/{teamId}/submit")


    public ResponseEntity<String> submit(@PathVariable int teamId) {
        Long uid = getCurrentUserId();
        if (uid == null) return unauthorized();
        return ResponseEntity.ok(teamService.submitTeamForApporal(teamId, uid));
    }

    @Operation(summary = "Admin duyệt hoặc từ chối TEAM_SUBMISSION",
            description = "APPROVED → Team.status=APPROVED, mã mời bị vô hiệu hoá")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/submission/{requestId}/review")
    public ResponseEntity<String> adminReview(
            @PathVariable Long requestId,
            @RequestParam boolean approve) {
        return ResponseEntity.ok(teamService.adminReviewTeam(requestId, approve));
    }










//    private Integer getUid(String authHeader) {
//        try {
//            String token = authHeader.substring(7);
//            String username = jwtUtil.extracUserName(token);
//            return userRepository.findByUsername(username)
//                    .map(u -> Math.toIntExact(u.getId()))
//                    .orElse(null);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return  null ;
//        }


    private Long getCurrentUserId() {
        try {
            String username = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            return userRepository.findByFullName(username)
                    .map(User::getId)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(401).build();
    }


}

