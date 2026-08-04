package com.talkkia.api.entity;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.Date;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: store user likes
 */
@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class TFitUserLikes {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private Long beLikedUserId;
    private String stepDate;
}
