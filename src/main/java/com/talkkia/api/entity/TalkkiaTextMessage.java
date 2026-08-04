package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

//this class may be deleted most probably
@Node("TextMessage")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TalkkiaTextMessage {
    
    @Id
    private long id;
    private String sentTime;
    private String receivedTime;
    private boolean status;
    private String content;
    
    @Relationship("SENDER")
    public User sender;
    @Relationship("RECEIVER")
    public User receiver; 
}
