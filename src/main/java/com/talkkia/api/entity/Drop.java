package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drop {
    private Long id;
    private Long userId;
    private Long cityId;
    private String audioUrl;
    private String status;
    private Long confession;
    private Long funny;
    private Long opinion;
    private Long student;
    private Long random;
    private Long listen;
    private Long replyCount;
    private Instant expiresAt;
}
