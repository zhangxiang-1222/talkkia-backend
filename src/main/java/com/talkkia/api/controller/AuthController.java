package com.talkkia.api.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import com.talkkia.api.dto.drop.DeleteDropRequest;
import com.talkkia.api.services.DropService;
import com.talkkia.api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.talkkia.api.controller.UserController.CLIENT_TOKEN;

/**
 * @Author: Ethan
 * @Date: 2024/9/17
 * @Description: prompt api not login and no permission to access API
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private DropService dropService;

    @Autowired
    private UserService userService;

    @GetMapping("/noPermission")
    public ResponseEntity<Object> noPermission() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Unauthorized Access!");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/drops/delete")
    public ResponseEntity<Object> deleteDrop(
            @RequestHeader Map<String, String> headers,
            @RequestBody DeleteDropRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userIdValue = headers.get("userid");
            boolean authorized = CLIENT_TOKEN.equals(headers.get("client-token"))
                    && userIdValue != null
                    && userService.authenticateUser(
                            Long.parseLong(userIdValue),
                            headers.get("token")
                    );
            if (!authorized) {
                response.put("success", false);
                response.put("status", "ua00");
                response.put("message", "Unauthorized Access.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            dropService.deleteDrop(
                    Long.parseLong(userIdValue),
                    request
            );
            response.put("success", true);
            response.put("status", "dr00");
            response.put("message", "Drop deleted.");
            response.put("dropId", request.getDropId());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "dr01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
