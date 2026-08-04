package com.talkkia.api.controller;

import com.google.gson.Gson;
import static com.talkkia.api.controller.UserController.CLIENT_TOKEN;
import com.talkkia.api.services.GroupService;
import com.talkkia.api.view.GroupView;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.talkkia.api.services.UserService;
import java.util.HashMap;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/group")
@Slf4j
public class GroupController {

    @Autowired
    private GroupService groupService;
    @Autowired
    private UserService userService;

    /*most of these APIs are/will moved to wsgroupcontroller and should be delete from here.
    see APIs sheet for reference.*/
    @PostMapping("/creategroup")
    private Map createGroup(@RequestHeader("userid") Long userId, @RequestBody Map payload) {
        String title = payload.get("title").toString();
        List<Long> invitation = (List) payload.get("invitation");
        GroupView gv = groupService.createGroup(userId, title, invitation);
        Map<String, Object> response = new HashMap<>();
        if (gv != null) {
            response.put("success", true);
            response.put("status", "gp01");//group is created
            response.put("type", "group");
            response.put("data", gv);
        } else {
            response.put("success", false);
            response.put("status", "gp00");//group is not created
            response.put("type", "group");
            response.put("message", "group is not created.");
        }
        return response;
    }

    @PostMapping("/joingroup")
    private Map joinGroup(@RequestBody String values) {
        Long userId = Long.parseLong(new Gson().fromJson(values, Map.class).get("userid").toString());
        Long groupId = Long.parseLong(new Gson().fromJson(values, Map.class).get("groupid").toString());
        System.out.println(userId);
        System.out.println(groupId);
        return groupService.joinGroup(userId, groupId);
    }

    @PostMapping("/leavegroup")
    private Boolean leaveGroup(@RequestBody String values) {
        Long userId = Long.parseLong(new Gson().fromJson(values, Map.class).get("userid").toString());
        Long groupId = Long.parseLong(new Gson().fromJson(values, Map.class).get("groupid").toString());
        return groupService.leaveGroup(userId, groupId);
    }

    @PostMapping("/addgroupadmin")
    private String addGroupAdmin(@RequestBody String values) {
        Long ownerId = Long.parseLong(new Gson().fromJson(values, Map.class).get("ownerid").toString());
        Long adminId = Long.parseLong(new Gson().fromJson(values, Map.class).get("adminid").toString());
        Long groupId = Long.parseLong(new Gson().fromJson(values, Map.class).get("groupid").toString());
        return groupService.addGroupAdmin(ownerId, adminId, groupId);
    }

    @PostMapping("/removegroupadmin")
    private String removeGroupAdmin(@RequestBody String values) {
        Long ownerId = Long.parseLong(new Gson().fromJson(values, Map.class).get("ownerid").toString());
        Long adminId = Long.parseLong(new Gson().fromJson(values, Map.class).get("adminid").toString());
        Long groupId = Long.parseLong(new Gson().fromJson(values, Map.class).get("groupid").toString());
        return groupService.removeGroupAdmin(ownerId, adminId, groupId);
    }

    @PostMapping("/removegroupmember")
    private String removeGroupMember(@RequestBody String values) {
        Long adminId = Long.parseLong(new Gson().fromJson(values, Map.class).get("adminid").toString());
        Long memberId = Long.parseLong(new Gson().fromJson(values, Map.class).get("memberid").toString());
        Long groupId = Long.parseLong(new Gson().fromJson(values, Map.class).get("groupid").toString());
        return groupService.removeGroupMember(adminId, memberId, groupId);
    }

    @PostMapping("/changegroupapproval")
    private String changeGroupApproval(@RequestBody String values) {
        Long adminId = Long.parseLong(new Gson().fromJson(values, Map.class).get("adminid").toString());
        Long groupId = Long.parseLong(new Gson().fromJson(values, Map.class).get("groupid").toString());
        return groupService.changeGroupApproval(adminId, groupId);
    }

    @GetMapping("/getgroup")
    private ResponseEntity<Object> getGroup(@RequestHeader Map<String, String> headers, @RequestBody Map<String, Long> groupId) {
        Map<String, Object> response = new HashMap<>();
        try {
            String clientToken = headers.get("client-token");
            String token = headers.get("token");
            Long userId = Long.parseLong(headers.get("userid"));
            if (clientToken.equals(CLIENT_TOKEN) && userService.authenticateUser(userId, token)) {
                response.put("success", true);
                response.put("status", "gp07");
                response.put("type", "group");
                response.put("data", groupService.getGroup(groupId.get("groupid")));
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("type", "group");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            System.out.println(e.getCause() + e.getMessage());
            response.put("success", false);
            response.put("status", "se00");
            response.put("type", "group");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getallgroups")
    private ResponseEntity<Object> getMyGroups(@RequestHeader Map<String, String> headers) {
        Map<String, Object> response = new HashMap<>();
        try {
            String clientToken = headers.get("client-token");
            String token = headers.get("token");
            Long userId = Long.parseLong(headers.get("userid"));
            if (clientToken.equals(CLIENT_TOKEN) && userService.authenticateUser(userId, token)) {
                response.put("success", true);
                response.put("status", "gp08");
                response.put("type", "group");
                response.put("data", groupService.getMyGroups(userId));
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("type", "group");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            System.out.println(e.getCause() + e.getMessage());
            response.put("success", false);
            response.put("status", "se00");
            response.put("type", "group");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/getMembers")
    public ResponseEntity getMembers(@RequestHeader Map<String, String> headers,@RequestBody Map<String, Long> groupId){
        Map<String, Object> response = new HashMap<>();
        try{
            String clientToken = headers.get("client-token");
            String token = headers.get("token");
            Long userId = Long.parseLong(headers.get("userid"));
            if (clientToken.equals(CLIENT_TOKEN) && userService.authenticateUser(userId, token)) {
                response.put("success", true);
                response.put("status", "gp200");
                response.put("type", "group");
                response.put("data", groupService.getGroupMembers(groupId.get("groupId")));
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("status", "gp500");
            response.put("type", "group");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }catch(Exception e){
            log.error("error",e);
            response.put("success", false);
            response.put("status", "se00");
            response.put("type", "group");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
