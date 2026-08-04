package com.talkkia.api.controller;

import com.talkkia.api.dto.upload.CreateUploadRequest;
import com.talkkia.api.dto.upload.CreateUploadResponse;
import com.talkkia.api.dto.upload.CompleteUploadRequest;
import com.talkkia.api.dto.drop.DropListPageRequest;
import com.talkkia.api.dto.drop.DropListPageResponse;
import com.talkkia.api.dto.drop.IncrementCityListenRequest;
import com.talkkia.api.dto.drop.IncrementCityListenResponse;
import com.talkkia.api.dto.drop.LatestDropsPageRequest;
import com.talkkia.api.dto.drop.LatestDropsPageResponse;
import com.talkkia.api.dto.reply.ChildReplyListPageResponse;
import com.talkkia.api.dto.reply.ChildReplyListRequest;
import com.talkkia.api.dto.reply.CompleteReplyUploadRequest;
import com.talkkia.api.dto.reply.CreateReplyUploadRequest;
import com.talkkia.api.dto.reply.CreateReplyUploadResponse;
import com.talkkia.api.dto.reply.DeleteReplyRequest;
import com.talkkia.api.dto.reply.ReplyListPageResponse;
import com.talkkia.api.dto.reply.ReplyListRequest;
import com.talkkia.api.dto.reply.ReplyReactionRequest;
import com.talkkia.api.dto.reply.ReplyReactionResponse;
import com.talkkia.api.entity.City;
import com.talkkia.api.entity.Drop;
import com.talkkia.api.services.CityService;
import com.talkkia.api.services.DropModerationPublisher;
import com.talkkia.api.services.DropModerationResultCache;
import com.talkkia.api.services.DropReplyService;
import com.talkkia.api.services.DropService;
import com.talkkia.api.services.R2UploadService;
import com.talkkia.api.services.ReplyModerationPublisher;
import com.talkkia.api.services.ReplyModerationResultCache;
import com.talkkia.api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.talkkia.api.controller.UserController.CLIENT_TOKEN;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private R2UploadService uploadService;

    @Autowired
    private CityService cityService;

    @Autowired
    private DropService dropService;

    @Autowired
    private DropModerationPublisher moderationPublisher;

    @Autowired
    private DropModerationResultCache resultCache;

    @Autowired
    private UserService userService;

    @Autowired
    private DropReplyService dropReplyService;

    @Autowired
    private ReplyModerationPublisher replyModerationPublisher;

    @Autowired
    private ReplyModerationResultCache replyModerationResultCache;

    @PostMapping("/upload-url")
    public ResponseEntity<Object> createUploadUrl(
            @RequestHeader() Map<String, String> headers,
            @RequestBody CreateUploadRequest request) {

        Map<String, String> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException("Upload request is required");
            }

            String userId = headers.get("userid");
            if (headers.get("client-token").equals(CLIENT_TOKEN) &&
                    userService.authenticateUser(Long.parseLong(userId), headers.get("token"))) {
                City city = cityService.resolveCity(
                        request.getCountryCode(),
                        request.getCityCode(),
                        request.getCityName()
                );
                CreateUploadResponse uploadResponse = uploadService.createUploadUrl(
                        userId,
                        request,
                        city.getId()
                );
                Drop drop = dropService.createInitialDrop(
                        Long.parseLong(userId),
                        city.getId(),
                        uploadResponse.getObjectKey()
                );
                uploadResponse.setDropId(drop.getId());
                return ResponseEntity.ok(uploadResponse);
            }
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (IllegalArgumentException e) {
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/upload-complete")
    public ResponseEntity<Object> completeUpload(
            @RequestHeader() Map<String, String> headers,
            @RequestBody CompleteUploadRequest request) {

        Map<String, Object> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException("Upload completion request is required");
            }

            String userId = headers.get("userid");
            if (headers.get("client-token").equals(CLIENT_TOKEN) &&
                    userService.authenticateUser(Long.parseLong(userId), headers.get("token"))) {
                boolean completed = dropService.markUploadCompleted(
                        Long.parseLong(userId),
                        request.getDropId()
                );
                if (!completed) {
                    response.put("success", false);
                    response.put("status", "dr01");
                    response.put("message", "Drop not found, expired, or unavailable.");
                    return ResponseEntity.status(404).body(response);
                }

                moderationPublisher.publish(request.getDropId());

                response.put("success", true);
                response.put("status", "dr00");
                response.put("message", "Upload completed.");
                response.put("dropId", request.getDropId());
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access.");
            return ResponseEntity.status(401).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/replies/upload-url")
    public ResponseEntity<Object> createReplyUploadUrl(
            @RequestHeader Map<String, String> headers,
            @RequestBody CreateReplyUploadRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException("Reply upload request is required");
            }

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
                return ResponseEntity.status(401).body(response);
            }

            CreateReplyUploadResponse uploadResponse =
                    dropReplyService.createUploadUrl(
                            Long.parseLong(userIdValue),
                            request
                    );
            return ResponseEntity.ok(uploadResponse);
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "rp01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/replies/upload-complete")
    public ResponseEntity<Object> completeReplyUpload(
            @RequestHeader Map<String, String> headers,
            @RequestBody CompleteReplyUploadRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException(
                        "Reply upload completion request is required"
                );
            }

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
                return ResponseEntity.status(401).body(response);
            }

            boolean completed = dropReplyService.markUploadCompleted(
                    Long.parseLong(userIdValue),
                    request.getReplyId()
            );
            if (!completed) {
                response.put("success", false);
                response.put("status", "rp01");
                response.put(
                        "message",
                        "Reply not found, expired, or unavailable."
                );
                return ResponseEntity.status(404).body(response);
            }

            replyModerationPublisher.publish(request.getReplyId());
            response.put("success", true);
            response.put("status", "rp00");
            response.put("message", "Reply upload completed.");
            response.put("replyId", request.getReplyId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/drop-moderation-result")
    public ResponseEntity<Object> getDropModerationResult(
            @RequestHeader Map<String, String> headers,
            @RequestParam Long dropId
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userIdValue = headers.get("userid");
            boolean authorized = CLIENT_TOKEN.equals(headers.get("client-token"))
                    && userIdValue != null
                    && userService.authenticateUser(Long.parseLong(userIdValue), headers.get("token"));

            if (!authorized) {
                response.put("success", false);
                response.put("status", "ua00");
                response.put("message", "Unauthorized Access.");
                return ResponseEntity.status(401).body(response);
            }

            Long userId = Long.parseLong(userIdValue);
            if (!dropService.isOwnedAndAvailable(userId, dropId)) {
                response.put("success", false);
                response.put("status", "dr01");
                response.put("message", "Drop not found, expired, or unavailable.");
                return ResponseEntity.status(404).body(response);
            }

            String result = resultCache.get(dropId);
            response.put("success", true);
            response.put("dropId", dropId);
            response.put("value", result);

            if ("1".equals(result)) {
                response.put("state", "approved");
            } else if ("0".equals(result)) {
                response.put("state", "rejected");
            } else {
                response.put("state", "processing");
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/reply-moderation-result")
    public ResponseEntity<Object> getReplyModerationResult(
            @RequestHeader Map<String, String> headers,
            @RequestParam Long replyId
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
                return ResponseEntity.status(401).body(response);
            }

            Long userId = Long.parseLong(userIdValue);
            if (!dropReplyService.isOwnedAndAvailable(userId, replyId)) {
                response.put("success", false);
                response.put("status", "rp01");
                response.put(
                        "message",
                        "Reply not found, expired, or unavailable."
                );
                return ResponseEntity.status(404).body(response);
            }

            String result = replyModerationResultCache.get(replyId);
            if (result == null) {
                Boolean approved = dropReplyService.findModerationDecision(replyId);
                if (approved != null) {
                    result = approved ? "1" : "0";
                    replyModerationResultCache.record(replyId, approved);
                }
            }
            response.put("success", true);
            response.put("replyId", replyId);
            response.put("value", result);
            if ("1".equals(result)) {
                response.put("state", "approved");
            } else if ("0".equals(result)) {
                response.put("state", "rejected");
            } else {
                response.put("state", "processing");
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/replies")
    public ResponseEntity<Object> getReplies(
            @RequestHeader Map<String, String> headers,
            @RequestBody ReplyListRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException("Reply list request is required");
            }

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
                return ResponseEntity.status(401).body(response);
            }

            ReplyListPageResponse pageResponse =
                    dropReplyService.findTopLevelReplies(
                            Long.parseLong(userIdValue),
                            request.getDropId(),
                            request.getSort(),
                            request.getPage(),
                            request.getSize()
                    );
            return ResponseEntity.ok(pageResponse);
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "rp01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/replies/children")
    public ResponseEntity<Object> getChildReplies(
            @RequestHeader Map<String, String> headers,
            @RequestBody ChildReplyListRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException(
                        "Child reply list request is required"
                );
            }

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
                return ResponseEntity.status(401).body(response);
            }

            ChildReplyListPageResponse pageResponse =
                    dropReplyService.findChildReplies(
                            Long.parseLong(userIdValue),
                            request.getParentReplyId(),
                            request.getPage(),
                            request.getSize()
                    );
            return ResponseEntity.ok(pageResponse);
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "rp01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/replies/reaction")
    public ResponseEntity<Object> setReplyReaction(
            @RequestHeader Map<String, String> headers,
            @RequestBody ReplyReactionRequest request
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
                return ResponseEntity.status(401).body(response);
            }

            ReplyReactionResponse reactionResponse =
                    dropReplyService.setReaction(
                            Long.parseLong(userIdValue),
                            request
                    );
            return ResponseEntity.ok(reactionResponse);
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "rp01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/replies/delete")
    public ResponseEntity<Object> deleteReply(
            @RequestHeader Map<String, String> headers,
            @RequestBody DeleteReplyRequest request
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
                return ResponseEntity.status(401).body(response);
            }

            dropReplyService.deleteReply(
                    Long.parseLong(userIdValue),
                    request
            );
            response.put("success", true);
            response.put("status", "rp00");
            response.put("message", "Reply deleted.");
            response.put("replyId", request.getReplyId());
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "rp01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("status", "rp02");
            response.put("message", e.getMessage());
            return ResponseEntity.status(409).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/drops")
    public ResponseEntity<Object> getLatestDrops(
            @RequestHeader Map<String, String> headers,
            @RequestBody DropListPageRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException("Drop list request is required");
            }

            String userIdValue = headers.get("userid");
            boolean authorized = CLIENT_TOKEN.equals(headers.get("client-token"))
                    && userIdValue != null
                    && userService.authenticateUser(Long.parseLong(userIdValue), headers.get("token"));
            if (!authorized) {
                response.put("success", false);
                response.put("status", "ua00");
                response.put("message", "Unauthorized Access.");
                return ResponseEntity.status(401).body(response);
            }

            DropListPageResponse pageResponse = dropService.findLatestAvailableDrops(
                    Long.parseLong(userIdValue),
                    request.getCityId(),
                    request.getCityCode(),
                    Boolean.TRUE.equals(request.getIncludeCityOptions()),
                    request.getPage()
            );
            return ResponseEntity.ok(pageResponse);
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "ct01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/latest-drops")
    public ResponseEntity<Object> getLatestUserDrops(
            @RequestHeader Map<String, String> headers,
            @RequestBody(required = false) LatestDropsPageRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String userIdValue = headers.get("userid");
            boolean authorized = CLIENT_TOKEN.equals(headers.get("client-token"))
                    && userIdValue != null
                    && userService.authenticateUser(Long.parseLong(userIdValue), headers.get("token"));
            if (!authorized) {
                response.put("success", false);
                response.put("status", "ua00");
                response.put("message", "Unauthorized Access.");
                return ResponseEntity.status(401).body(response);
            }

            LatestDropsPageResponse pageResponse = dropService.findLatestUserDrops(
                    Long.parseLong(userIdValue),
                    request == null ? null : request.getPage()
            );
            return ResponseEntity.ok(pageResponse);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/city-listens/increment")
    public ResponseEntity<Object> incrementCityListen(
            @RequestHeader Map<String, String> headers,
            @RequestBody IncrementCityListenRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (request == null) {
                throw new IllegalArgumentException("City listen request is required");
            }

            String userIdValue = headers.get("userid");
            boolean authorized = CLIENT_TOKEN.equals(headers.get("client-token"))
                    && userIdValue != null
                    && userService.authenticateUser(Long.parseLong(userIdValue), headers.get("token"));
            if (!authorized) {
                response.put("success", false);
                response.put("status", "ua00");
                response.put("message", "Unauthorized Access.");
                return ResponseEntity.status(401).body(response);
            }

            Long listen = cityService.incrementListen(request.getCityId());
            return ResponseEntity.ok(new IncrementCityListenResponse(request.getCityId(), listen));
        } catch (NoSuchElementException e) {
            response.put("success", false);
            response.put("status", "ct01");
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("status", "aa00");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(500).body(response);
        }
    }
}
