package com.minhtung.hackathon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreEditDetailResponse {

    private Long id;                  // requestId
    private String teamName;
    private Long teamId;
    private String round;
    private Long submissionId;
    private LocalDateTime time;
    private String status;            // "pending" | "approved" | "rejected"

    private RequestedByDTO requestedBy;
    private String reason;

    private List<CriteriaDTO> criteria;
    private Long affectedCriteriaId;

    private List<JudgeDTO> judges;
    private ImpactDTO impact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestedByDTO {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CriteriaDTO {
        private Long id;
        private String name;
        private Double weight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JudgeDTO {
        private Long id;
        private String name;
        private Boolean isSender;
        private Map<Long, Double> scores;          // Key: criteriaId (Long)
        private Map<Long, Double> proposedScores;  // Key: criteriaId (Long)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactDTO {
        private Integer totalTeams;
        private ImpactDetail before;
        private ImpactDetail after;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImpactDetail {
        private Double judgeTotal;
        private Double teamScore;
        private Integer teamRank;
    }
}