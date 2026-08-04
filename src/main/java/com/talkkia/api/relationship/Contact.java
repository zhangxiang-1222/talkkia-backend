package com.talkkia.api.relationship;

import com.talkkia.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contact {
    
    @Id
    @GeneratedValue
    private Long id;
    @Property("hidemypost")
    private Boolean hideMyPost;
    @Property("hidetheirpost")
    private Boolean hideTheirPost;
    @TargetNode
    private User user;

    public Contact(Boolean hideMyPost, Boolean hideTheirPost, User user) {
        this.hideMyPost = hideMyPost;
        this.hideTheirPost = hideTheirPost;
        this.user = user;
    }
}
