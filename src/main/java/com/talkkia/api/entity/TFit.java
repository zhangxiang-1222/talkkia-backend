package com.talkkia.api.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.util.Date;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: store user's steps and likes in someday
 */
@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class TFit {

    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private String stepDate;
    private Integer steps;
    private Integer likes;
}
