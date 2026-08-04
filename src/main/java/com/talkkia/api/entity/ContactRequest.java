package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import javax.swing.*;

import com.fasterxml.jackson.annotation.JsonProperty;

@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactRequest {

    @Id
    @GeneratedValue
    private Long id;
    private String timestamp;
    private String greeting;
    private boolean hidemypost;
    private boolean hidetheirpost;
    private String source;
    @JsonProperty("isNearby")
    private boolean isNearby;
    @Relationship(type="FROM", direction=Relationship.Direction.INCOMING)
    private User from;
    @Relationship(type="TO", direction=Relationship.Direction.OUTGOING)
    private User to;
}
