package com.minhtung.hackathon.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamDto {
    private String teamName ;
    private List<Long> memberId ;



}
