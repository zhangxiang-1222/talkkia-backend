package com.talkkia.api.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessAccount {

    @Id
    private Long id;
    private String title;
    private String contact;
    private String email;
    private String website;
    private String address;
    private String profilePicture;
    private Boolean verified = false;
    @Relationship(type = "BUSINESS_OWNER", direction = Relationship.Direction.INCOMING)
    public User owner;
    @Relationship(type = "OPERATOR", direction = Relationship.Direction.INCOMING)
    public Set<User> operators = new HashSet<>();
    @Relationship(type = "FOLLOWER", direction = Relationship.Direction.INCOMING)
    public List<User> followers = new ArrayList<>();

}
