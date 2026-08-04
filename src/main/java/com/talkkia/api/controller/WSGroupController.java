package com.talkkia.api.controller;

import com.talkkia.api.config.JwtTokenUtil;
import com.talkkia.api.services.GroupService;
import com.talkkia.api.services.UserService;
import com.talkkia.api.view.GroupView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static com.talkkia.api.controller.UserController.CLIENT_TOKEN;

//see GroupController and APIs sheet and implement other APIs accordingly is ws group controller.
@Controller
@Slf4j
public class WSGroupController {

    @Autowired
    private GroupService groupService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    SimpMessagingTemplate simpTemplate;
    @Autowired
    private UserService userService;

    Map<String, Object> headers = new HashMap<>();

    @MessageMapping("/group/creategroup")
    private void createGroup(@Payload Map payload, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        String title = payload.get("title").toString();
        List<Long> invitation = (List) payload.get("invitation");

        System.out.println("group created");
        GroupView gv = groupService.createGroup(Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString()), title, invitation);
        if (gv != null) {
            response.put("success", true);
            response.put("status", "gp01");//group is succesfully created
            response.put("type", "group");
            response.put("data", gv);
        } else {
            response.put("success", false);
            response.put("status", "gp00");//group is not created
            response.put("type", "group");
            response.put("message", "group is not created.");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @MessageMapping("/group/joingroup")
    private void joinGroup(@Header("groupid") Long groupId, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        try {
            response = groupService.joinGroup(Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString()), groupId);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "gp00");//exeption thrown
            response.put("type", "group");
            response.put("message", "You can't join this group at the moment.");
        }

        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @MessageMapping("/group/leavegroup")
    private void leaveGroup(@Header("groupid") Long groupId, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        if (groupService.leaveGroup(Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString()), groupId)) {
            response.put("success", true);
            response.put("status", "gp04");//group has been leaved
            response.put("type", "group");
            response.put("message", "group has been leaved.");
        } else {
            response.put("success", false);
            response.put("status", "gp00");//exeption thrown
            response.put("type", "group");
            response.put("message", "You can't leave this group at the moment.");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), groupService.joinGroup(Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString()), groupId), headers);
    }

    @MessageMapping("/group/addgroupadmin")
    private void addGroupAdmin(SimpMessageHeaderAccessor accessor, @Payload Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long ownerId = Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString());
            Long adminId = Long.parseLong(payload.get("adminid"));
            Long groupId = Long.parseLong(payload.get("groupid"));
            response.put("success", true);
            response.put("type", "group");
            response.put("status", "gp06");
            response.put("message", groupService.addGroupAdmin(ownerId, adminId, groupId));
        } catch (Exception e) {
            System.out.println(e.getCause() + e.getMessage());
            response.put("success", false);
            response.put("status", "se00");
            response.put("type", "group");
            response.put("message", "System error!");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @MessageMapping("/group/getgroup")
    private void getGroup(SimpMessageHeaderAccessor accessor, @Payload Map<String, Long> groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("status", "gp07");
            response.put("type", "group");
            response.put("data", groupService.getGroup(groupId.get("groupid")));
        } catch (Exception e) {
            System.out.println(e.getCause() + e.getMessage());
            response.put("success", false);
            response.put("status", "se00");
            response.put("type", "group");
            response.put("message", "Group error!");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @MessageMapping("/group/getmembers")
    private void getMembers(SimpMessageHeaderAccessor accessor, @Payload Map<String, Long> groupId) {
        Map<String, Object> response = new HashMap<>();
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @PostMapping("/group/getMemberUsers")
    private void getMemberUsers(SimpMessageHeaderAccessor accessor, @Payload Map<String, Long> groupId){
        Map<String, Object> response = new HashMap<>();
        try{
            response.put("success", true);
            response.put("status", "gpuser");
            response.put("type", "group");
            response.put("data", groupService.getGroupMemberUsers(groupId.get("groupId")));
        }catch(Exception e){
            log.error("error",e);
            response.put("success", false);
            response.put("status", "se00");
            response.put("type", "group");
            response.put("message", "System error!");
        }
        simpTemplate.convertAndSend("/queue/group/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

}
