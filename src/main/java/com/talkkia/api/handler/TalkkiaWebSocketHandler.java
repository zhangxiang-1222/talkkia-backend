package com.talkkia.api.handler;

import com.google.gson.Gson;
import com.talkkia.api.config.JwtTokenUtil;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.services.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/*most of the code in this class is not required so, should be reviewed and 
deleted if not required in the last. since we moved to stomp protocol most of 
these functions.*/

public class TalkkiaWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    UserService userService;

    private final List<WebSocketSession> wsSessions = new ArrayList<>();
    //the following Set is synchronized but above list is not. asynch Set would be better
    //private final Set<WebSocketSession> wsSessions = Collections.synchronizedSet(new HashSet<WebSocketSession>());
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        /*System.out.println(wsSessions.size() + " : " + session.toString());
        System.out.println(session.getHandshakeHeaders().get("token"));
        System.out.println("handler called...");
        if (userRepository.existsByMobile(session.getHandshakeHeaders().get("mobile").get(0))) {
            if (session.getHandshakeHeaders().get("token").get(0)
                    .equals(jwtTokenUtil.generateToken(userRepository
                            .findOneByMobile(session.getHandshakeHeaders().get("mobile").get(0)).get().toString()))) {
                //session.getAttributes().put("sessionId", session.getHandshakeHeaders().get("mobile").get(0));
                System.out.println(session.getAttributes().toString());
                wsSessions.add(session);
                System.out.println(wsSessions.size());
                session.sendMessage(new TextMessage("your connection has been established..."));
                //session.sendMessage(new TextMessage(consumer.consumeMessage(session.getAttributes().get("sessionId").toString()).toString()));
            } else {
                System.out.println("disconnect this session");
                session.close();
            }
        } else {
            if (session.getHandshakeHeaders().get("token").get(0).equals(jwtTokenUtil.generateToken(session.getHandshakeHeaders().get("mobile").get(0)))) {
                session.getAttributes().put("sessionId", session.getHandshakeHeaders().get("mobile").get(0));
                wsSessions.add(session);
            } else {
                System.out.println("disconnect this session");
                session.close();
            }
        }*/
        
        System.out.println("Session ID: " + session.getId());
        System.out.println("connection established method called");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println(wsSessions.size() + session.getAttributes().toString());
        String receiver = new Gson().fromJson(message.getPayload(), Map.class).get("receiver").toString();
        for (WebSocketSession s : wsSessions) {
            if (s.getAttributes().get("sessionId").equals(receiver)) {
                s.sendMessage(message);
                System.out.println("message sent to receiver");
                return;
            }
        }
        //for message broker etc...
        String sender = session.getAttributes().get("sessionId").toString();
        String content = new Gson().fromJson(message.getPayload(), Map.class).get("message").toString();
        //userService.sendMessage(sender, receiver, content);
        System.out.println("message sent to the queue");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        //wsSessions.remove(session);
        System.out.println("connection closed");
    }

}
