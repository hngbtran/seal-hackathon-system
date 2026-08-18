package com.minhtung.hackathon.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "score_edit_request_detail")
@Data
public class ScoreEditRequestDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_request_id", nullable = false)
    private SystemRequest systemRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", nullable = false)
    private Criterion criterion;

    @Column(nullable = false)
    private double newScore;

    @Column(columnDefinition = "TEXT")
    private String newComment;
}