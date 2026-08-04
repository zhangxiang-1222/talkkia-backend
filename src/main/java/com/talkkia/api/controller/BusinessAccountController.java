package com.talkkia.api.controller;

import com.google.gson.Gson;
import com.talkkia.api.entity.BusinessAccount;
import com.talkkia.api.services.BusinessAccountService;
import com.talkkia.api.services.UserService;
import com.talkkia.api.view.BusinessAccountView;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/businessaccount")
public class BusinessAccountController {
    
    @Autowired
    BusinessAccountService businessAccountService;
    @Autowired
    UserService userService;
    
    /*most of these apis are moving to ws controller see APIs sheet
    the api can be shorten to create in ws controller. and similarly all other 
    following apis.*/
    @PostMapping("/createbusinessaccount")
    private ResponseEntity<Object> createBusinessAccount(@RequestHeader() Map<String, String> headers, @RequestBody BusinessAccount account) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessAccountService.createBusinessAccount(Long.parseLong(headers.get("userid")), account));
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

    @PostMapping("/addbusinessaccountoperator")
    private ResponseEntity<Object> addBusinessAccountOperator(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessAccountService.addBusinessAccountOperator(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("operatorid")), Long.parseLong(headers.get("businessaccountid"))));
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
    
    @PostMapping("/removebusinessaccountoperator")
    private ResponseEntity<Object> removeBusinessAccountOperator(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessAccountService.removeBusinessAccountOperator(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("operatorid")), Long.parseLong(headers.get("businessaccountid"))));
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
    
    @GetMapping("/getbusinessaccount")
    private ResponseEntity<Object> getBusinessAccount(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessAccountService.getBusinessAccount(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("businessaccountid"))));
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
    
    @GetMapping("/followbusinessaccount")
    private ResponseEntity<Object> followBusinessAccount(@RequestHeader() Map<String, String> headers) {
        Map<String, String> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals("xyz") && userService.authenticateUser(Long.parseLong(headers.get("userid")), headers.get("token"))) {
                return ResponseEntity.ok(businessAccountService.followBusinessAccount(Long.parseLong(headers.get("userid")), Long.parseLong(headers.get("businessaccountid"))));
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
