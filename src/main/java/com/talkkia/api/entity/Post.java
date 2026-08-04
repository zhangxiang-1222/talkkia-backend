package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import java.util.ArrayList;
import java.util.List;

@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue
    private Long id;
    private String postText;
    private String[] postPicVid;
    private String dateTime;
    @Relationship(type = "POST_BY",direction = Relationship.Direction.INCOMING)
    private User postCreator;
    @Relationship(type = "LIKE",direction = Relationship.Direction.INCOMING)
    private List<User> likes = new ArrayList<>();
    @Relationship(type = "DISLIKE",direction = Relationship.Direction.INCOMING)
    private List<User> dislikes = new ArrayList<>();
    @Relationship(type = "COMMENT",direction = Relationship.Direction.INCOMING)
    private List<Comment> comments = new ArrayList<>();
}
