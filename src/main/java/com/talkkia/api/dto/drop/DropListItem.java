package com.talkkia.api.dto.drop;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DropListItem {
    private Long id;
    private Long userId;
    private String username;
    private String avatarUrl;
    private Long cityId;
    private String cityName;
    private String audioUrl;
    private Long confession;
    private Long funny;
    private Long opinion;
    private Long student;
    private Long random;
    private Long listen;
    private Long replyCount;
    private Boolean liked;
    private Boolean laughed;
    private Instant createdAt;
}
