package com.talkkia.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

//to consume OTP and other messages if required
@Component
public class Consumer {

    @Autowired
    JmsTemplate jmsTemplate;    
    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
    
    //@JmsListener(destination = "queue/test")
    public void consumeMessage(Message msg) throws JMSException {
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("message consumer");
        try {
            //Object msg1 = jmsTemplate.receiveAndConvert("/queue/test");
            System.out.println(((TextMessage) msg).getText());
        }catch(JmsException e){
            System.out.println(e);  
        }
    }

    //@JmsListener(destination = "/topic/chat")
    public Message messageConsumer(String sender) {
        try {
            Message msg = null;//jmsTemplate.receive("/topic/chat");
            System.out.println("message: " + msg);
            return msg;
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
