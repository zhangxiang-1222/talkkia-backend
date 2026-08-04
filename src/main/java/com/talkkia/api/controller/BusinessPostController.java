package com.talkkia.api.controller;

import com.talkkia.api.entity.BusinessPost;
import com.talkkia.api.services.BusinessPostService;
import com.talkkia.api.services.UserService;
import com.talkkia.api.view.BusinessPostView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businesspost")
public class BusinessPostController {

    @Autowired
    BusinessPostService businessPostService;
    @Autowired
    UserService userService;

    /*actually according to my intention all those heavy APIs which involve more 
    data transfer should be done on http rest and other light work should be 
    done on ws controller apis.*/
    @PostMapping(value = "/create")
    public ResponseEntity<Object> createPost(@RequestHeader() Map<String, String> headers, @RequestBody BusinessPost post) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessPostService.createPost(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("business-accountid")), post));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @DeleteMapping("/deletepost")
    public ResponseEntity<Object> deletePost(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessPostService.deletePost(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("business-postid"))));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getpost")
    public ResponseEntity<Object> getPost(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessPostService.getPost(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("business-postid"))));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/getallposts")
    public ResponseEntity<Object> getAllPost(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessPostService.getAllPost(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("business-accountid"))));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/likepost")
    public ResponseEntity<Object> likePost(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessPostService.likePost(Long.parseLong(headers.get("business-postid")), Boolean.parseBoolean(headers.get("like"))));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/sharepost")
    public ResponseEntity<Object> sharePost(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessPostService.sharePost(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("postid"))));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

}
