package com.talkkia.api.config;

import com.talkkia.api.handler.TalkkiaHandshakeInterceptor;
import com.talkkia.api.handler.TalkkiaWebSocketHandler;
import com.talkkia.api.repositories.UserRepository;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.tcp.TcpOperations;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

//unnecessary code should be removed on release.
@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
//@ServerEndpoint("/talkkiawebsocket")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.artemis.host}")
    private String brokerUrl;
    @Value("${spring.artemis.port}")
    private int brokerPort;
    @Value("${spring.artemis.user}")
    private String brokerUser;
    @Value("${spring.artemis.password}")
    private String brokerPassword;

    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    //@Autowired
    //ActiveMQProperties activeMQProperties;

    @Nullable
    TcpOperations<byte[]> tcpClient;

    /*
    we will create customized HTTPSessionHanshakeInterceptors to make the security 
    more strengthen. for help and detailed docs
    https://docs.spring.io/spring-framework/docs/4.3.x/spring-framework-reference/html/websocket.html
    wshr.addHandler(xxx).setAllowedOrigins("") can be called to allow different origins
     
    @Override //without stomp
    public void registerWebSocketHandlers(WebSocketHandlerRegistry wshr) {
        wshr.addHandler(getTalkkiaWebSocketHandler(), "/chat")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    } */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket")
                .addInterceptors(new TalkkiaHandshakeInterceptor(userRepository, jwtTokenUtil))
                .setAllowedOrigins("*");
        System.out.println("end point registered...");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry brokerRegistry) {
        //config.enableStompBrokerRelay("/group", "/chat").setTcpClient(createTcpClient());
        //config.setApplicationDestinationPrefixes("/chat");

        brokerRegistry.enableStompBrokerRelay("/topic", "/queue")
                .setRelayHost(brokerUrl)
                .setRelayPort(brokerPort)
                .setClientLogin(brokerUser)
                .setClientPasscode(brokerPassword)
                .setSystemLogin(brokerUser)
                .setSystemPasscode(brokerPassword);
        //.setTcpClient(tcpClient);
        brokerRegistry.setApplicationDestinationPrefixes("/app");
        brokerRegistry.setUserDestinationPrefix("/user");
        System.out.println("ws configured...");
        System.out.println(brokerUrl);
    }

    public void setTcpClient(@Nullable TcpOperations<byte[]> tcpClient) {
        this.tcpClient = tcpClient;
    }

    private TcpOperations<byte[]> createTcpClient() {
        return this.tcpClient;
    }

    @Bean
    public WebSocketHandler getTalkkiaWebSocketHandler() {
        System.out.println("handler adding...");
        return new TalkkiaWebSocketHandler();
    }
    
    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration){
       registration.setMessageSizeLimit(512 * 1024);
       registration.setSendBufferSizeLimit(512 * 1024);
        System.out.println("message size limit increased to 512kb");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebsocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(512 * 1024);
        container.setMaxBinaryMessageBufferSize(1000 * 1024);
        System.out.println("container created...");
        return container;
    }
}
