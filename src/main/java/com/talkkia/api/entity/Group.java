package com.talkkia.api.entity;

import com.talkkia.api.relationship.GroupMember;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

@Node
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {

    @Id
    private Long id;
    private String title;
    private String picture;
    private String notice;
    private Boolean approval;
    private String provenance;
    @Relationship(type = "MEMBER", direction = Relationship.Direction.INCOMING)
    public Set<GroupMember> members = new HashSet<>();
    @Relationship(type = "JOIN_REQUEST", direction = Relationship.Direction.INCOMING)
    public Set<User> join_requests = new HashSet<>();

}
