package com.talkkia.api.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    
    @Autowired
    JmsTemplate template;
    
    @Value("${jms.queue}")
    String queue;
    
    public String sendMessage(String destination, String message){
        template.convertAndSend(destination, message);
        return "message sent";
    }
    
    //@JmsListener(destination = "/topic/talkkia")
    public void messageConsumer(String message) {
        try {
            
            System.out.println(message);
            
        } catch (Exception ex) {
            
        }
    }
    
    //@JmsListener(destination = "talkkia")
    public void queueConsumer(String message) {
        try {
            
            System.out.println(message);
            
        } catch (Exception ex) {
            
        }
    }
    
}
