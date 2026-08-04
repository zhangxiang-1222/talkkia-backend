package com.talkkia.api.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class WSController {
    
    @MessageMapping("/talkkia")
    public void wsTest(String txt){
        System.out.println(txt + " from opera");
    }
    
}
