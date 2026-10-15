package com.talkkia.api.controller;

import static com.talkkia.api.controller.UserController.CLIENT_TOKEN;

import com.talkkia.api.dto.contact.ContactSyncRequest;
import com.talkkia.api.dto.contact.ContactSyncResponse;
import com.talkkia.api.services.ContactSyncConfigurationException;
import com.talkkia.api.services.ContactSyncService;
import com.talkkia.api.services.UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/contacts")
public class ContactsController {

    private final UserService userService;
    private final ContactSyncService contactSyncService;

    public ContactsController(UserService userService, ContactSyncService contactSyncService) {
        this.userService = userService;
        this.contactSyncService = contactSyncService;
    }

    @PostMapping("/sync")
    public ResponseEntity<Object> syncContacts(
            @RequestHeader Map<String, String> headers,
            @RequestBody ContactSyncRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        Long userId = null;
        try {
            userId = parseAuthorizedUserId(headers);
            if (userId == null) {
                response.put("success", false);
                response.put("status", "ua00");
                response.put("message", "Unauthorized Access.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            ContactSyncResponse syncResponse = contactSyncService.sync(userId, request);
            response.put("success", true);
            response.put("status", "cs00");
            response.put("isDisputed", false);
            response.put("data", syncResponse);
            return ResponseEntity.ok(response);
        } catch (ContactSyncConfigurationException e) {
            log.error("Contact sync is unavailable for user {}", userId, e);
            response.put("success", false);
            response.put("status", "cs02");
            response.put("message", "Contact sync is temporarily unavailable.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "cs01");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Contact sync failed for user {}", userId, e);
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private Long parseAuthorizedUserId(Map<String, String> headers) {
        if (!CLIENT_TOKEN.equals(headers.get("client-token"))) {
            return null;
        }
        String userIdValue = headers.get("userid");
        if (userIdValue == null) {
            return null;
        }
        try {
            Long userId = Long.parseLong(userIdValue);
            return userService.authenticateUser(userId, headers.get("token")) ? userId : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
