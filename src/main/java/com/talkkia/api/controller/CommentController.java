package com.talkkia.api.controller;

import com.google.gson.Gson;
import com.talkkia.api.services.CommentService;
import com.talkkia.api.services.UserService;
import com.talkkia.api.view.CommentView;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

/*this code is written by another developer and need to review to map and match 
to our code convention and standard. same as postcontroller*/
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    UserService userService;

    @GetMapping("getallcommentbypost")
    public ResponseEntity<Object> getAllCommentByPostId(@RequestHeader Map<String, String> headers, Pageable pageable) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(commentService.getAllCommentByPostId(pageable, Long.parseLong(headers.get("postid")), Long.parseLong(headers.get("userid"))));
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

    @GetMapping("/getcomment")
    public ResponseEntity<Object> getCommentById(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(commentService.getCommentById(Long.parseLong(headers.get("commentid")), Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("postid"))));
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

    @PostMapping(value = "/createcomment")
    public ResponseEntity<Object> createComment(@RequestHeader Map<String, String> headers, @RequestBody String values) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                String commentTxt = new Gson().fromJson(values, Map.class).get("text").toString();
                String commentPic = new Gson().fromJson(values, Map.class).get("picture").toString();
                return ResponseEntity.ok(commentService.createComment(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("postid")), commentTxt, commentPic));
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

    /*Depricated for now
    @PutMapping(value = "/update/{commentId}")
    public Comment updateComment(@RequestHeader("userId") Long userId,
            @PathVariable Long commentId,
            @RequestBody Comment comment) {
        return commentService.updateComment(userId, commentId, comment);
    }
     */
    @DeleteMapping(value = "/deletecomment")
    public ResponseEntity<Object> deleteComment(@RequestHeader Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(commentService.deleteComment(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("commentid"))));
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
