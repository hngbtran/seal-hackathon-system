package com.minhtung.hackathon.service;

import com.minhtung.hackathon.dto.request.HandleViolationRequestDto;
import com.minhtung.hackathon.dto.request.ScoreEditRequestDto;
import com.minhtung.hackathon.dto.response.ViolationResponseDto;
import com.minhtung.hackathon.entity.*;
import com.minhtung.hackathon.enums.TeamStatus;
import com.minhtung.hackathon.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemRequestService {
    private final SubmissionRepository submissionRepository;
    private final RoundRepository roundRepository;
    private final SystemRequestRepository systemRequestRepository;
    private final TeamResultRepository teamResultRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final JudgeScoreRepository judgeScoreRepository;
    private final  CriterionRepository criterionRepository;

    public List<ViolationResponseDto> getPendingViolations() {
        List<SystemRequest> requests = systemRequestRepository.findByTypeAndStatus(
                SystemRequest.RequestType.FLAG_VIOLATION,
                SystemRequest.RequestStatus.PENDING
        );

        return requests.stream().map(req -> {
            // 1. Lấy thông tin bài nộp để lấy Tên Đội
            Submission submission = submissionRepository.findById(req.getReferenceId()).orElse(null);
            String teamName = (submission != null && submission.getTeam() != null)
                    ? submission.getTeam().getName()
                    : "Đội #" + req.getReferenceId();

            // 2. Dùng RoundRepository để lấy Tên Vòng từ req.getRoundId()
            String roundName = "Chưa xác định";

            roundName = roundRepository.findById(req.getRoundId())
                    .map(Round::getName) // Hoặc getRoundName() tùy field trong Entity Round của bạn
                    .orElse("Vòng " + req.getRoundId());


            return ViolationResponseDto.builder()
                    .id(req.getId())
                    .submissionId(req.getReferenceId())
                    .teamName(teamName)
                    .round(roundName)
                    .judgeName(req.getSender() != null ? req.getSender().getFullName() : "N/A")
                    .time(req.getSentAt())
                    .reason(req.getMessage())
                    .build();
        }).collect(Collectors.toList());
    }


    @Transactional
    public void handleViolation(Long requestId, HandleViolationRequestDto dto) {
        // 1. Tìm SystemRequest vi phạm
        SystemRequest request = systemRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu vi phạm id: " + requestId));

        if (request.getType() != SystemRequest.RequestType.FLAG_VIOLATION) {
            throw new RuntimeException("Yêu cầu này không phải là báo cáo vi phạm!");
        }

        if (request.getStatus() != SystemRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý trước đó!");
        }

        // 2. Cập nhật thông tin xử lý vào SystemRequest
        request.setStatus(dto.getStatus()); // DTO đã là Enum nên gán trực tiếp
        request.setHandleMessage(dto.getHandleMessage());
        request.setUpdatedAt(LocalDateTime.now());
        systemRequestRepository.save(request);

        // 3. Nếu ACCEPTED (Loại đội thi) -> set isPassed = false
        // ✅ So sánh Enum bằng == hoàn toàn chính xác trong Java!
        if (dto.getStatus() == SystemRequest.RequestStatus.ACCEPTED) {
            Submission submission = submissionRepository.findById(request.getReferenceId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Bài nộp (Submission) tương ứng"));

            if (submission.getTeam() == null) {
                throw new RuntimeException("Bài nộp không gắn với Đội thi nào");
            }

            Long teamId = submission.getTeam().getId();
            Long roundId = request.getRoundId();
            Team team=teamRepository.findById(teamId).orElse(null);
            if (team == null) {
                throw new IllegalArgumentException("Team Not found");
            }
            team.setStatus(TeamStatus.BANNED);
            // Tìm TeamResult của Đội trong Vòng thi đó
            TeamResult teamResult = teamResultRepository.findByTeamIdAndRoundId(teamId, roundId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kết quả (TeamResult) của đội trong vòng thi này"));

            // Đánh dấu rớt/loại
            teamResult.setPassed(false);
            teamResult.setUpdatedAt(LocalDateTime.now());
            teamResultRepository.save(teamResult);
        }
    }



    /**
     * 1. GIÁM KHẢO GỬI YÊU CẦU SỬA ĐIỂM
     */
    @Transactional
    public void createScoreEditRequest(long userId, ScoreEditRequestDto dto) {
        User judge = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin Giám khảo"));

        // Tìm bảng điểm gốc của giám khảo này cho bài submissionId
        JudgeScore judgeScore = judgeScoreRepository
                .findBySubmissionIdAndJudgeAssignment_User_Id(dto.getSubmissionId(), judge.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng điểm chính thức của giám khảo cho bài thi này"));

        // Tạo SystemRequest (Phần Vỏ)
        SystemRequest request = new SystemRequest();
        request.setSender(judge);
        request.setReferenceId(judgeScore.getId()); // Lưu ID của JudgeScore gốc
        request.setReferenceType(SystemRequest.ReferenceType.JUDGE_SCORE);
        request.setType(SystemRequest.RequestType.SCORE_EDIT_REQUEST);
        request.setStatus(SystemRequest.RequestStatus.PENDING);
        request.setMessage(dto.getReason()); // Lý do xin sửa điểm

        // Nhận xét tổng thể mới truyền từ FE (lưu tạm vào handleMessage hoặc 1 field tạm)
        request.setHandleMessage(dto.getComment());
        request.setSentAt(LocalDateTime.now());

        // Map danh sách các tiêu chí bị lệch cần sửa (Phần Ruột)
        if (dto.getDetails() != null && !dto.getDetails().isEmpty()) {
            List<ScoreEditRequestDetail> details = dto.getDetails().stream().map(dDto -> {
                Criterion criterion = criterionRepository.findById(dDto.getCriterionId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tiêu chí có ID: " + dDto.getCriterionId()));

                ScoreEditRequestDetail detail = new ScoreEditRequestDetail();
                detail.setSystemRequest(request);
                detail.setCriterion(criterion);
                detail.setNewScore(dDto.getScore());
                detail.setNewComment(dDto.getComment());
                return detail;
            }).collect(Collectors.toList());

            request.setScoreEditDetails(details);
        }

        // Lưu toàn bộ request (Cascade.ALL sẽ tự lưu các chi tiết ScoreEditRequestDetail)
        systemRequestRepository.save(request);
    }


}