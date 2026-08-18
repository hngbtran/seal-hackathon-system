package com.minhtung.hackathon.controller;

import com.minhtung.hackathon.dto.request.HandleScoreEditRequestDto;
import com.minhtung.hackathon.dto.request.HandleViolationRequestDto;
import com.minhtung.hackathon.dto.request.ScoreEditRequestDto;
import com.minhtung.hackathon.dto.response.ScoreEditDetailResponse;
import com.minhtung.hackathon.dto.response.ViewTeamListRespone;
import com.minhtung.hackathon.repository.UserRepository;
import com.minhtung.hackathon.security.JwtUtil;
import com.minhtung.hackathon.service.RoundService;
import com.minhtung.hackathon.service.SystemRequestService;
import com.minhtung.hackathon.service.TeamResultService;
import com.minhtung.hackathon.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system-requests")
@RequiredArgsConstructor
public class SystemRequestController {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TeamService teamService;
    private final SystemRequestService systemRequestService;

    // API Lấy danh sách báo cáo vi phạm
    @GetMapping("/violations")
    public ResponseEntity<?> getViolationRequests() {
        return ResponseEntity.ok(systemRequestService.getPendingViolations());
    }


    // API xử lý báo cáo vi phạm
    @PutMapping("/violations/{id}/handle")
    public ResponseEntity<Void> handleViolation(
            @PathVariable("id") Long requestId,
            @RequestBody HandleViolationRequestDto dto) {

        systemRequestService.handleViolation(requestId, dto);
        return ResponseEntity.ok().build(); // Trả 200 OK rỗng để FE không bị dính lỗi parse JSON
    }


    @PostMapping("/{submissionId}/request-edit")
    public ResponseEntity<?> requestEditScore(
            @PathVariable long submissionId,
            @RequestBody ScoreEditRequestDto requestDto,
            @RequestHeader("Authorization") String auth // Hoặc cách bạn lấy User đang đăng nhập
    ) {
        Integer uid = getUid(auth);
        if (uid == null) {

            return unauthorized();
        }
        // Đảm bảo submissionId nhất quán
        requestDto.setSubmissionId(submissionId);

        systemRequestService.createScoreEditRequest(uid.longValue(), requestDto);
        return ResponseEntity.ok(Map.of("message", "Gửi yêu cầu thành công"));
    }


    // API Lấy danh sách tóm tắt Yêu cầu chỉnh sửa điểm
    @GetMapping("/score-edits")
    public ResponseEntity<?> getScoreEditRequests() {
        return ResponseEntity.ok(systemRequestService.getPendingScoreEditRequests());
    }


    // lay getScoreEditDetail
    @GetMapping("/score-edits/{id}")
    public ResponseEntity<ScoreEditDetailResponse> getScoreEditDetail(@PathVariable Long id) {
        return ResponseEntity.ok(systemRequestService.getScoreEditRequestDetail(id));
    }


    @PutMapping("/score-edits/{id}")
    public ResponseEntity<?> handleScoreEdit(
            @PathVariable Long id,
            @RequestBody HandleScoreEditRequestDto dto,
            @RequestHeader("Authorization") String auth
    ) {
        Integer uid = getUid(auth);
        if (uid == null) {
            return unauthorized();
        }
        systemRequestService.handleScoreEditRequest(id, dto, uid.longValue());
        return ResponseEntity.ok().build();
    }


    private Integer getUid(String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            return userRepository.findByEmail(email)
                    .map(u -> Math.toIntExact(u.getId()))
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(401).body("Token không hợp lệ");
    }


}