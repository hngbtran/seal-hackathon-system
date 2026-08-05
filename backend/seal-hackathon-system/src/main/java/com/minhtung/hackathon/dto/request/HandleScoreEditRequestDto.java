package com.minhtung.hackathon.dto.request;

import lombok.Data;

@Data
public class HandleScoreEditRequestDto {
    private String action; // "approve" | "reject"
    private String note;
}