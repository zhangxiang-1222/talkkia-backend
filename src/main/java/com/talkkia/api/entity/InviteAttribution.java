package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("InviteAttribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteAttribution {

    @Id
    private String id;
    private String inviteeUserId;
    private String inviterUserId;
    private String context;
    private String targetId;
    private String status;
    private String createdAt;
}
