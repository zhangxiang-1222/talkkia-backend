package com.talkkia.api.config;

import javax.jms.JMSException;
import javax.jms.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//this was for testing can be removed on release.
@Component
@Slf4j
public class Listener {

//@JmsListener(destination = "/queue/test")
//@SendTo("outbound.queue")
public void receiveMessage(final Message message) throws JMSException{
    System.out.println("Message Received: " + message.getBody(String.class) + message.getJMSType() + message.getStringProperty("mobile"));
}
  
}
