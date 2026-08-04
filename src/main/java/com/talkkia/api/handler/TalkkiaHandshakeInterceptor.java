package com.talkkia.api.handler;

import com.talkkia.api.config.JwtTokenUtil;
import static com.talkkia.api.controller.UserController.CLIENT_TOKEN;
import com.talkkia.api.repositories.UserRepository;
import java.util.Map;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

public class TalkkiaHandshakeInterceptor implements HandshakeInterceptor {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public TalkkiaHandshakeInterceptor(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest shr, ServerHttpResponse shr1, WebSocketHandler wsh, Map<String, Object> map) {
        try {
            map.put("sessionId", shr.getHeaders().getOrEmpty("userid").get(0));
            if(!shr.getHeaders().getOrEmpty("client-token").get(0).equals(CLIENT_TOKEN)){
                System.out.println("Not supported yet.");
                return false;
            }
            if (userRepository.existsById(Long.parseLong(shr.getHeaders().getOrEmpty("userid").get(0)))) {
                if (jwtTokenUtil.generateToken(Long.parseLong(shr.getHeaders()
                        .getOrEmpty("userid").get(0))).equals(shr.getHeaders().getOrEmpty("token").get(0))) {
                    //wsh.afterConnectionEstablished();
                    return true;
                }
            }

        } catch (Exception e) {
            System.out.println("Unauthorized person is trying to access...");
            return false;
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest shr, ServerHttpResponse shr1, WebSocketHandler wsh, Exception excptn) {
        System.out.println("connection established");
        //can do some code here if required...
    }

}
