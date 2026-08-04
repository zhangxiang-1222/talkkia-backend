package com.talkkia.api.config;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

//@Configuration
public class ActivemMQConfig {

    //@Value("${spring.activemq.broker-url}")
    @Value("${spring.artemis.host}")
    private String brokerUrl;
    private String USERNAME = "talkkia";
    private String PASSWORD = "talkkia2021";

    //@Bean
    public ConnectionFactory connectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        activeMQConnectionFactory.setUserName(USERNAME);
        activeMQConnectionFactory.setPassword(PASSWORD);
        activeMQConnectionFactory.setTrustAllPackages(true);
        return activeMQConnectionFactory;
    }

    //@Bean
    public Connection createConnection() throws JMSException {
        Connection connection = connectionFactory().createConnection();
        System.out.println(brokerUrl);
        return connection;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        jmsTemplate.setConnectionFactory(connectionFactory());
        jmsTemplate.setExplicitQosEnabled(true);
        //jmsTemplate.setPubSubDomain(true); it is only required for topic not for queue
        return jmsTemplate;
    }

    // Serialize message content to json using TextMessage
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        return converter;
    }

    //copied for spring official
    @Bean
    public JmsListenerContainerFactory<?> myFactory(ConnectionFactory connectionFactory,
            DefaultJmsListenerContainerFactoryConfigurer configurer) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        // This provides all boot's default to this factory, including the message converter
        configurer.configure(factory, connectionFactory);
        // You could still override some of Boot's default if necessary.
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrency("1-5"); //at least one consumer is up and upto 5 consumers
        //can be created according to the load.
        factory.setMessageConverter(jacksonJmsMessageConverter());
        return factory;

    }

}
