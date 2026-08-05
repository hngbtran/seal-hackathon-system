package com.minhtung.hackathon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoreEditRequestSummaryResponse {
    private Long requestId;       // Dùng Long để khớp với ID của SystemRequest trong DB
    private String teamName;     // Tên đội thi
    private String judgeName;    // Tên giám khảo xin sửa điểm
    private LocalDateTime time;  // Thời gian gửi request
    private String reason;       // Lý do xin sửa điểm
}