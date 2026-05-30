package com.minhtung.hackathon.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponseDto {
    private long id ;
    private String name ;
    private String status ;
    private String inviteCode ;
    private int memberCount ;
}
