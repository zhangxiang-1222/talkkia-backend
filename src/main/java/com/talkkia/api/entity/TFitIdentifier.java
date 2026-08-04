package com.talkkia.api.entity;

import lombok.*;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

/**
 * @Author: Ethan
 * @Date: 2024/8/9
 * @Description: store users' TFit identifier
 */
@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class TFitIdentifier {

    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    /**
     *  0-closed 1-opened
     */
    private Integer isOpen;
}
