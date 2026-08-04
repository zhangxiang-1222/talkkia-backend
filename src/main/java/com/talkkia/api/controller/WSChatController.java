package com.talkkia.api.controller;

import com.talkkia.api.config.JwtTokenUtil;
import com.talkkia.api.entity.User;
import com.talkkia.api.services.UserService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/*policy about message saving is changed, so most of messages will be directly 
go to broker directly from sender and broker will pass to receiver without any
 intervention from spring server etc.*/
@Controller
public class WSChatController {

    @Autowired
    private UserService userService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    SimpMessagingTemplate simpTemplate;
    @Autowired
    JmsMessagingTemplate temp;
    @Autowired
    JmsTemplate template;

    Map<String, Object> headers = new HashMap<>();

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }

    @MessageMapping("/chat/register")
    @SendToUser("/queue/reply")
    public String registerUser(User user) {
        System.out.println(user);
        return jwtTokenUtil.generateToken(user.getId());
    }

}
