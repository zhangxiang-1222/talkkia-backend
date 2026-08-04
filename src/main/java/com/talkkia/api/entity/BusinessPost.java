package com.talkkia.api.entity;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessPost {

    @Id
    @GeneratedValue
    private Long id;
    private String postTitle;
    private String postText;
    private String[] postPicVid;
    private String dateTime;
    @Relationship(type = "POST_FROM", direction = Relationship.Direction.INCOMING)
    private BusinessAccount postFrom;
    @Relationship(type = "POST_BY", direction = Relationship.Direction.INCOMING)
    private User postCreator;
    private int likes;
    private int dislikes;
    private int shared;
    @Relationship(type = "COMMENT", direction = Relationship.Direction.INCOMING)
    private List<Comment> comments = new ArrayList<>();

}
