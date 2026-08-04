package com.talkkia.api.relationship;

import com.talkkia.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMember {

    @Id
    @GeneratedValue
    private Long id;
    private Type type;
    @TargetNode
    private User user;

    public GroupMember(Type type, User user) {
        this.type = type;
        this.user = user;
    }
}