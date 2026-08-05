package com.minhtung.hackathon.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ScoreEditRequestDto {
    private long submissionId;
    private String comment; // Nhận xét tổng thể mới
    private String reason;  // Lý do xin sửa
    private String status;  // Frontend gửi "SUBMITTED"
    private List<ScoreDetailEditDto> details;

    @Data
    public static class ScoreDetailEditDto {
        private long criterionId;
        private double score;
        private String comment;
    }
}