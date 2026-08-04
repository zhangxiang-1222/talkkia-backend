package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.data.neo4j.core.schema.Relationship.Direction;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Step {

    @Id
    @GeneratedValue
    private Long id;
    private Integer count;
    private Boolean status;
    @Relationship(type="STEPTAKER", direction=Direction.INCOMING)
    public User steptaker;

}
