package com.talkkia.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReplyModerationPublisher {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${reply.moderation.queue}")
    private String queueName;

    public void publish(Long replyId) {
        if (replyId == null || replyId <= 0) {
            throw new IllegalArgumentException("Reply ID is required");
        }
        jmsTemplate.convertAndSend(queueName, replyId.toString());
    }
}
