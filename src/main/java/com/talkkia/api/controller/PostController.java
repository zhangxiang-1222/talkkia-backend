package com.talkkia.api.controller;

import com.talkkia.api.entity.Post;
import com.talkkia.api.services.PostService;
import com.talkkia.api.services.UserService;
import java.util.HashMap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;

/*this code is written by another developer and need to review to map and match 
to our code convention and standard.*/
@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    private PostService postService;
    @Autowired
    UserService userService;
    private final String SORT_ID = "id";

    //page size can be varied.
    @GetMapping(value = "/getbyuser")
    public ResponseEntity<Object> getAllPostByUser(@PageableDefault(size = 2) Pageable pageable, @RequestHeader Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(postService.getAllPostsByUser(pageable, Long.parseLong(headers.get("creatorid"))));
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
    
    @GetMapping(value = "/getallposts")
    public ResponseEntity<Object> getAllPosts(@PageableDefault(size = 2) Pageable pageable, @RequestHeader Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(postService.getPosts(pageable, Long.parseLong(headers.get("userid"))));
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
    public ResponseEntity<Object> getPost(@RequestHeader Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(postService.getPost(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("postid"))));
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

    @Transactional
    @PostMapping(value = "/createpost")
    public ResponseEntity<Object> createPost(@RequestHeader Map<String, String> headers, @RequestBody Post post) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(postService.createPost(Long.parseLong(headers.get("userid")), post));
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

    @PutMapping("/update")
    public ResponseEntity<Object> updatePost(@RequestHeader Map<String, String> headers, @RequestBody Post post) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok().body(postService.updatePost(Long.parseLong(headers.get("userid")), post));
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response.toString());
        }
    }

    @Transactional
    @DeleteMapping(value = "/delete")
    public ResponseEntity<Object> deletePost(@RequestHeader Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
        if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
            return ResponseEntity.ok(postService.deletePost(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("postid"))));
        } else {
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        }
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response.toString());
        }

    }

    @PostMapping("/react")
    public ResponseEntity<Object> reactToPost(@RequestHeader Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
        if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
            return ResponseEntity.ok(postService.react(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("postid")), Boolean.parseBoolean(headers.get("like"))));
        } else {
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        }
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response.toString());
        }
    }

    /*Depricated
    @PostMapping(value = "/share")
    public Boolean getAllPostByUser(@RequestHeader("userId") Long userId,
                                    @RequestBody ShareRequest shareRequest){
        return postService.sharePost(userId,shareRequest);
    }*/
}
