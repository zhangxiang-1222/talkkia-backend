package com.talkkia.api.entity;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: store user Time Zone,for example:${userId},+8:00
 */
@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UserTimeZone {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    /**
     * Time ZoneId offset,such as:GMT+8,it will store +08:00
     */
    private String offset;
}