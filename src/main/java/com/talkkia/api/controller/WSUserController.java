package com.talkkia.api.controller;

import com.google.gson.JsonObject;
import com.talkkia.api.config.JwtTokenUtil;
import com.talkkia.api.constants.NearbyConstant;
import com.talkkia.api.entity.ContactRequest;
import com.talkkia.api.services.UserService;
import com.talkkia.api.utils.RedisCommonUtil;
import com.talkkia.api.view.ContactRequestView;
import com.talkkia.api.view.UserView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class WSUserController {

    @Autowired
    private UserService userService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;
    @Autowired
    SimpMessagingTemplate simpTemplate;
    @Autowired
    JmsMessagingTemplate temp;//is temporary can be removed
    @Autowired
    JmsTemplate template;//is temporary can be removed SimpMessagingTemplate is prefered


    private static final String DATE_FORMAT = "yyyyMMdd";

    /*
    header is using on client side to differentiate incoming message/value.
    there is one other option to create JMS/Stomp compatible message its body
    and header and then sent that message. OR assign the header to some default 
    header in stomp/broker message headers such as messageType.
     */
    Map<String, Object> headers = new HashMap<>();
    @Autowired
    private RedisCommonUtil redisCommonUtil;

    @MessageExceptionHandler
    //@SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        System.out.println(exception);
        //simpTemplate.convertAndSend("/queue/errors", exception);
        return exception.getMessage();
    }

    /*
    Change other methods also to this standard. But first need to define a 
    standard with front end requirements. Such as we can also add a status code
    to the returning payload as defined in rest apis. Also for now I pass 
    success, status, and other indicator values in headers and data in body. 
    But These can also be passed in body, as feasable for frond end.
     */
    @MessageMapping("/user/getuser")
    private void getUser(@Header("userid") String userId, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("Session: " + accessor + "User's sessionId: " + accessor.getSessionAttributes().get("sessionId"));
        UserView userView = userService.getUserById(Long.parseLong(userId));
        if (userView != null) {
            response.put("success", true);
            response.put("type", "user");
            response.put("status", "ua05");
            response.put("data", userView);
            response.put("from", "ID");
            simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
        } else {
            response.put("success", false);
            response.put("type", "user");
            response.put("status", "ua06");
            response.put("message", "user does not exist.");
            simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
        }
    }

    //change it same as above get user by id.
    @MessageMapping("/user/getuserbymobile")
    private void getUserByMobile(@Header("mobile") String mobile, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("mobile: " + mobile);
        UserView userView = userService.getUserByMobile(mobile);
        if (userView != null) {
            response.put("success", true);
            response.put("type", "user");
            response.put("status", "ua05");
            response.put("data", userView);
            response.put("from", "Mobile");
            simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
        } else {
            response.put("success", false);
            response.put("type", "user");
            response.put("status", "ua06");
            response.put("message", "user does not exist.");
            simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
        }
    }

    /**
     * added by Ethan
     * @param countryCode
     * @param identifyId
     * @param accessor
     */
    @MessageMapping("/user/getuserbyidandmobile")
    private void getUserByIdAndMobile(@Header("countryCode") String countryCode,
                                      @Header("identifyId") String identifyId,
                                      SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        log.info("countryCode:{}",countryCode);
        log.info("identifyId:{}",identifyId);
        Map<String, Object> userViewResult = userService.getUserByIdAndMobile(countryCode,identifyId);
        if (userViewResult.get("userView") != null) {
            UserView userView = (UserView) userViewResult.get("userView");
            String from = (String)userViewResult.get("from");
            response.put("success", true);
            response.put("type", "user");
            response.put("status", "ua05");
            response.put("data", userView);
            response.put("from", from);
            simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
        } else {
            response.put("success", false);
            response.put("type", "user");
            response.put("status", "ua06");
            response.put("message", "user does not exist.");
            simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
        }
    }

    @MessageMapping("/user/deleteuser")
    private void deleteUser(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("User's sessionId: " + accessor.getSessionAttributes().get("sessionId"));
        if (userService.deleteUser(Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString()))) {
            response.put("success", true);
            response.put("type", "user");
            response.put("status", "ua07");
            response.put("message", "Your account has been deleted.");
        } else {
            response.put("success", false);
            response.put("type", "user");
            response.put("status", "ua08");
            response.put("message", "Your account is not deleted at the moment.");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @MessageMapping("/user/contactrequest")
    private void contactRequest(@Header("receiver-id") String receiverId, @Payload ContactRequest request, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("contact request: " + accessor.getSessionAttributes().get("sessionId") + ", " + receiverId + ", " + request);
        try {
            if (!receiverId.equals(accessor.getSessionAttributes().get("sessionId").toString())) {
                Long userId = Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString());
                boolean sent = userService.contactRequest(userId, Long.parseLong(receiverId), request);
                if (sent) {
                    if (request.isNearby()) {
                        String usersKey = getUsersKey(userId);
                        boolean isNewUser = redisCommonUtil.sAddWithExpire(
                                usersKey,
                                receiverId,
                                NearbyConstant.REDIS_NEARBY_EXPIRE_SECONDS);
                        if (isNewUser) {
                            long nearbyCount = redisCommonUtil.sSize(usersKey);
                            response.put("nearbyCount", nearbyCount);
                        }
                    }
                    response.put("success", true);
                    response.put("status", "cr01");
                    response.put("type", "contact-request");
                    response.put("message", "Request has been sent.");
                } else {
                    response.put("success", false);
                    response.put("status", "cr03");
                    response.put("type", "contact-request");
                    response.put("message", "You already sent request to this person.");
                }
            } else {
                response.put("success", false);
                response.put("status", "cr03");
                response.put("type", "contact-request");
                response.put("message", "You can't send contact request to yourself.");
            }
        } catch (Exception e) {
            System.out.println(e);
            response.put("success", false);
            response.put("status", "cr00");
            response.put("type", "contact-request");
            response.put("message", "Request can not be sent at the moment.");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @MessageMapping("/user/getcontactrequest")
    private void getContactRequest(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("contact request list");
        List<ContactRequestView> crv = userService.getContactRequests(Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString()));
        if (!crv.isEmpty()) {
            response.put("success", true);
            response.put("status", "cr04");
            response.put("type", "contact-request");
            response.put("data", crv);
        } else {
            response.put("success", false);
            response.put("status", "cr05");
            response.put("type", "contact-request");
            response.put("message", "No new contact requests.");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    //accepting contact request
    //private void addContact(@Header("requestid") Long requestId, @Header("hidemypost") Boolean hideFrom, @Header("hidetheirpost") Boolean hideTheir, SimpMessageHeaderAccessor accessor) {
    @MessageMapping("/user/addcontact")
    private void addContact(@Payload Map payload, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("contact added");
        UserView uv = userService.addContact(Long.parseLong(payload.get("requestid").toString()), Boolean.parseBoolean(payload.get("hidemypost").toString()), Boolean.parseBoolean(payload.get("hidetheirpost").toString()));
        if (uv != null) {
            response.put("success", true);
            response.put("status", "cr07");
            response.put("type", "contact-request-accepted");
            response.put("data", uv);
        } else {
            response.put("success", false);
            response.put("status", "cr00");
            response.put("type", "contact-request-accepted");
            response.put("message", "contact is not added.");
        }
        System.out.println(uv);
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }

    @MessageMapping("/user/deletecontact")
    private void deleteContact(@Header("contactid") String contactId, SimpMessageHeaderAccessor accessor) {
        Map<String, Object> response = new HashMap<>();
        System.out.println("contact deletion " + contactId);
        if (userService.deleteContact(Long.parseLong(accessor.getSessionAttributes().get("sessionId").toString()), Long.parseLong(contactId))) {
            response.put("success", true);
            response.put("status", "cr07");
            response.put("type", "contact-request");
            response.put("message", "Contact has been deleted.");
        } else {
            response.put("success", false);
            response.put("status", "cr00");
            response.put("type", "contact-request");
            response.put("message", "contact is not deleted.");
        }
        simpTemplate.convertAndSend("/queue/" + accessor.getSessionAttributes().get("sessionId"), response);
    }


    /**
     * 生成已申请用户 Set Redis Key
     *
     * @param userId 用户 ID
     * @return Redis Key
     */
    public static String getUsersKey(Long userId) {
        return NearbyConstant.REDIS_NEARBY_REQUEST_USERS_PREFIX + userId + ":" + getTodayDate();
    }


    /**
     * 生成当天的日期字符串（UTC 时间）
     *
     * @return 日期字符串，格式: yyyyMMdd
     */
    public static String getTodayDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
}
