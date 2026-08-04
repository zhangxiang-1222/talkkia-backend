package com.talkkia.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class DropModerationPublisher {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Value("${drop.moderation.queue}")
    private String queueName;

    public void publish(Long dropId) {
        if (dropId == null || dropId <= 0) {
            throw new IllegalArgumentException("Drop ID is required");
        }

        jmsTemplate.convertAndSend(queueName, dropId.toString());
    }
}
