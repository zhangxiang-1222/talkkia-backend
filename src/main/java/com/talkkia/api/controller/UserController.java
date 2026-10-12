package com.talkkia.api.controller;

import com.google.gson.Gson;
import com.talkkia.api.config.AgoraConfig;
import com.talkkia.api.config.JwtTokenUtil;
import com.talkkia.api.controller.WSUserController;
import com.talkkia.api.entity.ContactRequest;
import com.talkkia.api.entity.User;
import com.talkkia.api.model.SocialLoginRequest;
import com.talkkia.api.services.UserService;
import com.talkkia.api.utils.RedisCommonUtil;
import com.talkkia.api.view.ContactRequestView;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import com.talkkia.api.view.UserView;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author noor
 */
@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    AgoraConfig agora;
    private User user;

    @Autowired
    private RedisCommonUtil redisCommonUtil;


    @Value("${nearby.request.count}")
    private Integer nearbyRequestCount;


    //client token is a secure long (~99 character) string token assigned to mobile app to restrict access from other sources such as browser, postman etc
    public static String CLIENT_TOKEN = "FxVTPQ_3xK2+@uJujm}_:MvN?ufxK=5nZWAw9dJ^>^wTKHcJ7mg=_t>+a?c}_5_)2b9m~CBZyzq-Fwr>7r_J#xqBr8VcMkEPzNr";

    //this should not be exposed to external users. this is only for interservices user authentication.
    @PostMapping("/authenticateuser")
    private boolean authenticateUser(@RequestBody Map<String, String> userValues) {
        Long userId = Long.parseLong(userValues.get("userid"));
        String token = userValues.get("token");
        return userService.authenticateUser(userId, token);
    }

    /*for now this is for testing and probably not required in the production.
    can be delete or down in the release.*/
    //@PostMapping("/gettoken")
    private String getUserToken(@RequestBody Map<String, Long> userId) {
        return userService.getUserToken(userId.get("userid"));
    }

    @PostMapping("/getAgoraToken")
    private ResponseEntity<Object>  getAgoraToken(@RequestHeader Map<String, String> headers, @RequestBody Map<String, String> request) {

        boolean tokenFlag = headers.getOrDefault("client-token", "null").equals(CLIENT_TOKEN);
        Long userId = Long.parseLong(headers.getOrDefault("userid", "null"));
        Long uid = Long.parseLong(headers.getOrDefault("uid", "null"));

        String requestToken = headers.getOrDefault("token", "111");
        boolean userAuthFlag = userService.authenticateUser(userId, requestToken);
        String cname = headers.getOrDefault("cname", "null");
        if (cname.equals("null"))  {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("cname not available");
        }
        if (tokenFlag && userAuthFlag) {
            String token = agora.getAgoraToken(uid,cname);
            System.out.println("token: " + token);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("token not available");
    }


    @PostMapping("/signup")
    public ResponseEntity<Object> signUp(@RequestHeader("client-token") String clientToken, @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        log.info("signup access...");
        log.info("Client token: {}", clientToken);

        try {
            if (clientToken.equals(CLIENT_TOKEN)) {
                String mobile = request.get("mobile");
                String email = request.get("email");

                // Validate input
                if (mobile == null || mobile.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("status", "ua07");
                    response.put("message", "Mobile number is required.");
                    return ResponseEntity.badRequest().body(response);
                }

                if (email == null || email.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("status", "ua08");
                    response.put("message", "Email is required.");
                    return ResponseEntity.badRequest().body(response);
                }

                log.info("Processing signup for mobile: {}, email: {}", mobile, email);

                int otp = Integer.valueOf(request.get("otp"));

                Boolean result = userService.signUp(mobile, otp, email);

                if (result) {
                    response.put("success", true);
                    response.put("status", "ua09");
                    response.put("message", "OTP sent to your email. Please check your inbox.");
                    return ResponseEntity.ok(response);
                } else {
                    response.put("success", false);
                    response.put("status", "ua10");
                    response.put("message", "Failed to send OTP. Please try again.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
            }

            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            log.error("Error in signup process", e);
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/otp")
    public ResponseEntity<Object> verifyOTP(@RequestHeader("client-token") String clientToken, @RequestBody Map<String, String> values) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (clientToken.equals(CLIENT_TOKEN)) {
                String mobile = values.get("mobile");
                String email = values.get("email");
                String otpStr = values.get("otp");

                if (mobile == null || mobile.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("status", "ua11");
                    response.put("message", "Mobile number is required.");
                    return ResponseEntity.badRequest().body(response);
                }

                if (email == null || email.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("status", "ua11");
                    response.put("message", "Email number is required.");
                    return ResponseEntity.badRequest().body(response);
                }

                if (otpStr == null || otpStr.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("status", "ua12");
                    response.put("message", "OTP is required.");
                    return ResponseEntity.badRequest().body(response);
                }

                int otp;
                try {
                    otp = Integer.parseInt(otpStr);
                } catch (NumberFormatException e) {
                    response.put("success", false);
                    response.put("status", "ua13");
                    response.put("message", "Invalid OTP format.");
                    return ResponseEntity.badRequest().body(response);
                }

                return ResponseEntity.ok(userService.verifyOTP(mobile, email, otp));
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            log.error("Error in OTP verification", e);
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    ///  社交媒体登录
    @PostMapping("/social-login")
    public ResponseEntity<Object> socialLogin(@RequestHeader("client-token") String clientToken, @RequestBody SocialLoginRequest values) {
        Map<String, Object> response = new HashMap<>();
        log.info("11social-login request: provider={}, email={} inviteCode= {}", values.getProvider(), values.getEmail(), values.getInviteCode());

        try {
            if (!clientToken.equals(CLIENT_TOKEN)) {
                response.put("success", false);
                response.put("status", "ua00");
                response.put("message", "Unauthorized Access!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            return ResponseEntity.status(HttpStatus.OK).body(userService.verifySocialLogin(values));
        } catch (Exception e) {
            log.error("Error in social-login", e);
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }





    @PostMapping(value = "/register", consumes = {
            MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public ResponseEntity<Object> registerUser(@RequestHeader() Map<String, String> headers,
                                               @RequestParam("user") String user,
                                               @RequestPart(value = "file", required = false) MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        User userJson = new Gson().fromJson(user, User.class);
        try {
            if (headers.get("client-token").equals(CLIENT_TOKEN) && headers.get("token").equals(jwtTokenUtil.generateToken(Long.parseLong(userJson.getMobile())))) {
                System.out.println("you are going to register");
                return ResponseEntity.ok(userService.registerUser(userJson, file));
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            System.out.println(e.getCause() + " : " + e.getMessage());
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping(value = "/update", consumes = {
        MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public ResponseEntity<Object> updateUser(@RequestHeader() Map<String, String> headers, @RequestParam("user") String user, @RequestParam(value = "file", required = false) MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (headers.get("client-token").equals(CLIENT_TOKEN) && headers.get("token").equals(jwtTokenUtil.generateToken(Long.parseLong(headers.get("userid"))))) {
                System.out.println("you are going to update your account");
                //user.setId(Long.parseLong(headers.get("userid")));
                return ResponseEntity.ok(userService.updateUser(Long.parseLong(headers.get("userid")), user, file));
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            System.out.println(e);
            response.put("success", false);
            response.put("status", "se00");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    //should be deleted from here and implement in ws controller.
    //not for end users. can be accessed from talkkia admin panel.
    //@GetMapping("/allusers")
    private List<UserView> getAllUsers() {
        return userService.getUsers();
    }

    //all those apis implemented in ws controller should be deleted from here. see API sheet for reference.
    @PostMapping("/getuser")
    private UserView getUser(@RequestBody Map<String, Long> userId) {
        return userService.getUserById(userId.get("userid"));
    }

    @PostMapping("/getuserbymobile")
    private UserView getUserByMobile(@RequestBody Map<String, String> mobile) {
        return userService.getUserByMobile(mobile.get("mobile"));
    }

    @PostMapping("/getcontactids")
    private List<Long> getContactIds(@RequestBody Map<String, Long> userId) {
        return userService.getContactsIds(userId.get("userid"));
    }

    @PostMapping("/deleteuser")
    private Boolean deleteUser(@RequestBody Map<String, Long> userId) {
        return userService.deleteUser(userId.get("userid"));
    }

    //@GetMapping("/deleteallusers")
    private String deleteAllUsers() {
        return userService.deleteAllUsers();
    }

    @PostMapping("/contactrequest")
    private Boolean contactRequest(@RequestHeader("sender-id") Long senderId, @RequestHeader("receiver-id") Long receiverId, @RequestBody ContactRequest request) {
        System.out.println(request);
        return userService.contactRequest(senderId, receiverId, request);
    }

    @PostMapping("/getcontactrequest")
    private List<ContactRequestView> getContactRequest(@RequestBody Map userId) {
        return userService.getContactRequests(Long.parseLong(userId.get("userid").toString()));
    }

    @PostMapping("/addcontact")
    private UserView addContact(@RequestBody() Map payload) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("contact added");
        System.out.println(payload);
        Long requestId = Long.parseLong(payload.get("requestid").toString());
        Boolean hidefrom = Boolean.parseBoolean(payload.get("hidemypost").toString());
        Boolean hidetheir = Boolean.parseBoolean(payload.get("hidetheirpost").toString());
        UserView uv = userService.addContact(requestId, hidefrom, hidetheir);
        if (uv != null) {
            response.put("success", true);
            response.put("status", "cr07");
            response.put("type", "contact-request");
            response.put("data", uv);
        } else {
            response.put("success", false);
            response.put("status", "cr00");
            response.put("type", "contact-request");
            response.put("message", "contact is not added.");
        }
        return null;
    }

    @PostMapping("/deletecontact")
    private Boolean deleteContact(@RequestBody String contact) {
        Long myId = Long.parseLong(new Gson().fromJson(contact, Map.class).get("myid").toString());
        Long contactId = Long.parseLong(new Gson().fromJson(contact, Map.class).get("contactid").toString());
        System.out.println(myId + " : " + contactId);
        return userService.deleteContact(myId, contactId);
    }

    @GetMapping("/getallcontacts")
    private ResponseEntity<Object> getAllContacts(@RequestHeader Map<String, String> headers) {
        Map<String, Object> response = new HashMap<>();
        try {

            String clientToken = headers.get("client-token");
            String token = headers.get("token");
            Long userId = Long.parseLong(headers.get("userid"));

            String usersKey = WSUserController.getUsersKey(userId);
            long nearbyCount = redisCommonUtil.sSize(usersKey);
            response.put("nearbyCount", nearbyCount);
            response.put("nearbyRequestCount", nearbyRequestCount);

            if (clientToken.equals(CLIENT_TOKEN) && userService.authenticateUser(userId, token)) {
                response.put("success", true);
                response.put("status", "ua08");
                response.put("type", "user");
                response.put("data", userService.getAllContacts(userId));
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("status", "ua00");
            response.put("type", "user");
            response.put("message", "Unauthorized Access!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("status", "se00");
            response.put("type", "user");
            response.put("message", "System error!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
