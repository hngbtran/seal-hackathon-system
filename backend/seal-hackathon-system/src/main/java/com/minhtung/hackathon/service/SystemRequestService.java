package com.minhtung.hackathon.service;

import com.minhtung.hackathon.dto.request.HandleScoreEditRequestDto;
import com.minhtung.hackathon.dto.request.HandleViolationRequestDto;
import com.minhtung.hackathon.dto.request.ScoreEditRequestDto;
import com.minhtung.hackathon.dto.response.ScoreEditDetailResponse;
import com.minhtung.hackathon.dto.response.ScoreEditRequestSummaryResponse;
import com.minhtung.hackathon.dto.response.ViolationResponseDto;
import com.minhtung.hackathon.entity.*;
import com.minhtung.hackathon.enums.AuditAction;
import com.minhtung.hackathon.enums.TeamStatus;
import com.minhtung.hackathon.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    private final AuditLogService  auditLogService;
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



    @Transactional(readOnly = true)
    public List<ScoreEditRequestSummaryResponse> getPendingScoreEditRequests() {
        // 1. Lấy danh sách các request xin chỉnh sửa điểm
        List<SystemRequest> requests = systemRequestRepository
                .findByTypeAndStatus(
                        SystemRequest.RequestType.SCORE_EDIT_REQUEST,
                        SystemRequest.RequestStatus.PENDING
                );

        // 2. Map sang DTO tóm tắt
        return requests.stream().map(req -> {
            // Lấy thông tin JudgeScore gốc thông qua referenceId để suy ra Đội thi
            String teamName = "N/A";
            if (req.getReferenceId() != 0) {
                teamName = judgeScoreRepository.findById(req.getReferenceId())
                        .map(js -> js.getSubmission().getTeam().getName())
                        .orElse("N/A");
            }

            // Tên giám khảo gửi request
            String judgeName = req.getSender() != null ? req.getSender().getFullName() : "N/A";

            return ScoreEditRequestSummaryResponse.builder()
                    .requestId(req.getId())
                    .teamName(teamName)
                    .judgeName(judgeName)
                    .time(req.getSentAt())
                    .reason(req.getMessage()) // req.getMessage() lưu lý do (dto.getReason())
                    .build();
        }).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public ScoreEditDetailResponse getScoreEditRequestDetail(Long requestId) {
        SystemRequest request = systemRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu id: " + requestId));

        if (request.getType() != SystemRequest.RequestType.SCORE_EDIT_REQUEST) {
            throw new RuntimeException("Yêu cầu này không phải yêu cầu sửa điểm!");
        }

        // referenceId lưu id của JudgeScore gốc (theo createScoreEditRequest)
        JudgeScore senderScore = judgeScoreRepository.findById(request.getReferenceId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng điểm gốc"));

        Submission submission = senderScore.getSubmission();
        Team team = submission.getTeam();
        Round round = submission.getRound();
        TeamResult teamResult = senderScore.getTeamResult();

        // Đề xuất sửa: criterionId -> điểm mới
        Map<Long, Double> proposedMap = request.getScoreEditDetails().stream()
                .collect(Collectors.toMap(d -> d.getCriterion().getId(), ScoreEditRequestDetail::getNewScore));

        // Rubric: lấy trực tiếp từ các tiêu chí mà chính JudgeScore này đã chấm
        List<Criterion> criteria = senderScore.getDetails().stream()
                .map(JudgeScoreDetail::getCriterion)
                .collect(Collectors.toList());

        // Danh sách giám khảo cùng chấm đội này trong vòng này
        List<JudgeScore> allScores = teamResult.getJudgeScores();

        List<ScoreEditDetailResponse.JudgeDTO> judgeDtos = allScores.stream().map(js -> {
            boolean isSender = js.getId() == senderScore.getId();
            Map<Long, Double> currentScores = js.getDetails().stream()
                    .collect(Collectors.toMap(d -> d.getCriterion().getId(), JudgeScoreDetail::getScore));

            return ScoreEditDetailResponse.JudgeDTO.builder()
                    .id(js.getJudgeAssignment().getUser().getId())
                    .name(js.getJudgeAssignment().getUser().getFullName())
                    .isSender(isSender)
                    .scores(currentScores)
                    .proposedScores(isSender ? proposedMap : null)
                    .build();
        }).collect(Collectors.toList());

        double judgeTotalBefore = senderScore.getTotalScore();
        double judgeTotalAfter = weightedTotal(senderScore, criteria, proposedMap);

        double teamScoreBefore = teamResult.getTotalScore();
        double teamScoreAfter = allScores.stream()
                .mapToDouble(js -> js.getId() == senderScore.getId() ? judgeTotalAfter : js.getTotalScore())
                .average().orElse(0);

        List<TeamResult> roundResults = teamResultRepository.findByRoundIdOrderByTotalScoreDesc(round.getId());
        int totalTeams = roundResults.size();
        int rankBefore = teamResult.getRanking();
        int rankAfter = recomputeRank(roundResults, team.getId(), teamScoreAfter);

        return ScoreEditDetailResponse.builder()
                .id(request.getId())
                .teamName(team.getName())
                .teamId(team.getId())
                .round(round.getName())
                .submissionId(submission.getId())
                .time(request.getSentAt())
                .status(mapStatus(request.getStatus()))
                .requestedBy(ScoreEditDetailResponse.RequestedByDTO.builder()
                        .id(request.getSender().getId())
                        .name(request.getSender().getFullName())
                        .build())
                .reason(request.getMessage())
                .criteria(criteria.stream().map(c -> ScoreEditDetailResponse.CriteriaDTO.builder()
                                .id(c.getId()).name(c.getName()).weight((double) c.getWeight()).build())
                        .collect(Collectors.toList()))
                .affectedCriteriaId(proposedMap.keySet().stream().findFirst().orElse(null))
                .judges(judgeDtos)
                .impact(ScoreEditDetailResponse.ImpactDTO.builder()
                        .totalTeams(totalTeams)
                        .before(ScoreEditDetailResponse.ImpactDetail.builder()
                                .judgeTotal(judgeTotalBefore).teamScore(teamScoreBefore).teamRank(rankBefore).build())
                        .after(ScoreEditDetailResponse.ImpactDetail.builder()
                                .judgeTotal(judgeTotalAfter).teamScore(teamScoreAfter).teamRank(rankAfter).build())
                        .build())
                .build();
    }

    // Tính lại totalScore có trọng số cho 1 JudgeScore, override bằng proposedMap nếu tiêu chí nằm trong đó
    private double weightedTotal(JudgeScore judgeScore, List<Criterion> criteria, Map<Long, Double> override) {
        Map<Long, Double> currentScores = judgeScore.getDetails().stream()
                .collect(Collectors.toMap(d -> d.getCriterion().getId(), JudgeScoreDetail::getScore));
        if (override != null) currentScores.putAll(override);

        double totalWeight = criteria.stream().mapToDouble(Criterion::getWeight).sum();
        double weightedSum = criteria.stream()
                .mapToDouble(c -> currentScores.getOrDefault(c.getId(), 0.0) * c.getWeight())
                .sum();
        return totalWeight == 0 ? 0 : weightedSum / totalWeight;
    }

    // Xếp hạng lại đội trong vòng, thay điểm đội đang xét bằng điểm giả định mới, giữ nguyên điểm các đội khác
    private int recomputeRank(List<TeamResult> roundResults, Long teamId, double newTeamScore) {
        List<Double> scores = roundResults.stream()
                .map(tr -> tr.getTeam().getId() == teamId ? newTeamScore : tr.getTotalScore())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        return scores.indexOf(newTeamScore) + 1;
    }

    private String mapStatus(SystemRequest.RequestStatus status) {
        return switch (status) {
            case PENDING -> "pending";
            case ACCEPTED -> "approved";
            case REJECTED -> "rejected";
            default -> status.name().toLowerCase();
        };
    }






    @Transactional
    public void handleScoreEditRequest(Long requestId, HandleScoreEditRequestDto dto, Long actorUserId) {
        SystemRequest request = systemRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu id: " + requestId));

        if (request.getType() != SystemRequest.RequestType.SCORE_EDIT_REQUEST) {
            throw new RuntimeException("Yêu cầu này không phải yêu cầu sửa điểm!");
        }
        if (request.getStatus() != SystemRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý trước đó!");
        }

        boolean approve = "approve".equalsIgnoreCase(dto.getAction());

        // Người thực hiện log = người đã gửi yêu cầu sửa điểm (giám khảo)
        User performedBy = request.getSender();

        request.setStatus(approve ? SystemRequest.RequestStatus.ACCEPTED : SystemRequest.RequestStatus.REJECTED);
        request.setHandleMessage(dto.getNote());
        request.setUpdatedAt(LocalDateTime.now());
        systemRequestRepository.save(request);

        JudgeScore senderScore = judgeScoreRepository.findById(request.getReferenceId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bảng điểm gốc"));

        if (!approve) {
            auditLogService.log(
                    "JudgeScore",
                    senderScore.getId(),
                    AuditAction.SCORE_EDITED,
                    "status",
                    "PENDING",
                    "REJECTED",
                    performedBy
            );
            return;
        }

        // ===== DUYỆT: áp dụng điểm mới =====
        Map<Long, Double> proposedMap = request.getScoreEditDetails().stream()
                .collect(Collectors.toMap(d -> d.getCriterion().getId(), ScoreEditRequestDetail::getNewScore));

        double oldJudgeTotal = senderScore.getTotalScore();

        for (JudgeScoreDetail detail : senderScore.getDetails()) {
            Long criterionId = detail.getCriterion().getId();
            if (proposedMap.containsKey(criterionId)) {
                detail.setScore(proposedMap.get(criterionId));
            }
        }

        List<Criterion> criteria = senderScore.getDetails().stream()
                .map(JudgeScoreDetail::getCriterion)
                .collect(Collectors.toList());
        double totalWeight = criteria.stream().mapToDouble(Criterion::getWeight).sum();
        double weightedSum = senderScore.getDetails().stream()
                .mapToDouble(d -> d.getScore() * d.getCriterion().getWeight())
                .sum();
        senderScore.setTotalScore(totalWeight == 0 ? 0 : weightedSum / totalWeight);
        senderScore.setUpdatedAt(LocalDateTime.now());
        judgeScoreRepository.save(senderScore);

        TeamResult teamResult = senderScore.getTeamResult();
        double newTeamScore = teamResult.getJudgeScores().stream()
                .mapToDouble(JudgeScore::getTotalScore)
                .average().orElse(0);
        teamResult.setTotalScore(newTeamScore);
        teamResult.setUpdatedAt(LocalDateTime.now());
        teamResultRepository.save(teamResult);

        Long roundId = teamResult.getRound().getId();
        List<TeamResult> roundResults = teamResultRepository.findByRoundIdOrderByTotalScoreDesc(roundId);
        for (int i = 0; i < roundResults.size(); i++) {
            roundResults.get(i).setRanking(i + 1);
        }
        teamResultRepository.saveAll(roundResults);

        auditLogService.log(
                "JudgeScore",
                senderScore.getId(),
                AuditAction.SCORE_EDITED,
                "totalScore",
                String.valueOf(oldJudgeTotal),
                String.valueOf(senderScore.getTotalScore()),
                performedBy
        );
    }

}