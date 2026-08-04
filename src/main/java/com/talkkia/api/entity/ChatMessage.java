package com.talkkia.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessage {

Long senderId;
String sentTime;
String receivedTime;
String content;
    
}
