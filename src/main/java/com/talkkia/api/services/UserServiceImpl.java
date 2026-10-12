package com.talkkia.api.services;

import com.google.gson.Gson;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.talkkia.api.config.Consumer;
import com.talkkia.api.config.DateTimeConverter;
import com.talkkia.api.config.JwtTokenUtil;
import com.talkkia.api.config.TwilioConfig;
import com.talkkia.api.relationship.Contact;
import com.talkkia.api.entity.ContactRequest;
import com.talkkia.api.entity.User;
import com.talkkia.api.model.OTP;
import com.talkkia.api.model.SocialLoginRequest;
import com.talkkia.api.repositories.ContactRequestRepository;
import com.talkkia.api.repositories.UserRepository;
import com.talkkia.api.view.ContactRequestView;
import com.talkkia.api.view.UserDTO;
import java.util.List;
import java.util.stream.Collectors;

import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.internal.shaded.io.netty.util.internal.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.talkkia.api.view.UserView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/*Services are self explanatery. start from the controller to see flow of the code 
and to know how it works.*/
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContactRequestRepository contactRequestRepository;
    @Autowired
    private FileService fileService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JmsTemplate template;
    @Autowired
    private SimpMessagingTemplate simpTemplate;
    @Autowired
    private Consumer consumer;
    @Autowired
    private TwilioConfig twilio;
    // stomp headers for borker messages
    Map<String, Object> headers = new HashMap<>();
    // map for keeping otp
    Map<String, OTP> otps = new HashMap<>();
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserInviteService userInviteService;

    @Override
    public List<UserView> getUsers() {
        return userRepository.findAllById().get();
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Override
    public String signIn(String mobile) {
        return null;// producer.sendOTP(mobile);
    }

    @Override
    public Boolean signUp(String mobile, int otp, String email) {
        try {
            log.info("SignUp called with mobile: {}, email: {}", mobile, email);

            // Generate OTP (use provided OTP or generate random one)
            int generatedOtp = (otp != 0) ? otp : ThreadLocalRandom.current().nextInt(1000, 9999);
            log.info("Generated OTP: {}", generatedOtp);

            // Create OTP model and store it
            OTP otpModel = new OTP(generatedOtp, System.currentTimeMillis());
            otps.put(mobile, otpModel);

            boolean emailSent = emailService.sendOtpEmail(email, generatedOtp);
            if (emailSent) {
                log.info("OTP email sent successfully to: {}", email);
                return true;
            } else {
                log.error("Failed to send OTP email to: {}", email);
                // Remove OTP from memory if email failed
                otps.remove(mobile);
                return false;
            }

        } catch (Exception e) {
            log.error("Error during signup process for mobile: {}, email: {}", mobile, email, e);
            // Clean up on error
            otps.remove(mobile);
            return false;
        }
    }

    @Override
    public Map verifyOTP(String mobile, String email, int otp) {
        Map<String, Object> response = new HashMap<>();
        String normalizedEmail = email == null ? "" : email.trim();

        log.info("Verifying OTP for mobile: {}, provided OTP: {}", mobile, otp);

        if (otps.containsKey(mobile)) {
            OTP record = otps.get(mobile);

            if (record.getOtp() == otp && !record.isExpired()) {
                otps.remove(mobile);

                if (userRepository.existsByMobile(mobile)) {
                    UserView user = userRepository.findOneByMobile(mobile).orElseThrow();
                    response.put("success", true);
                    response.put("status", "ua02");
                    response.put("token", jwtTokenUtil.generateToken(user.getId()));
                    response.put("data", user);
                    return response;
                } else if (!normalizedEmail.isEmpty() && userRepository.existsByEmail(normalizedEmail)) {
                    User user = userRepository.findByEmail(normalizedEmail).orElse(null);
                    if (user != null) {
                        if (user.getMobile() == null || user.getMobile().trim().isEmpty()
                                || "null".equalsIgnoreCase(user.getMobile().trim())) {
                            user.setMobile(mobile);
                            user = userRepository.save(user);
                        }
                        UserView userView = userRepository.findOneById(user.getId()).orElse(null);
                        response.put("success", true);
                        response.put("status", "ua02");
                        response.put("token", jwtTokenUtil.generateToken(user.getId()));
                        response.put("data", userView);
                        return response;
                    }
                } else {
                    response.put("success", true);
                    response.put("status", "ua01");
                    response.put("token", jwtTokenUtil.generateToken(Long.parseLong(mobile)));
                    response.put("message", "New User");
                    return response;
                }
            } else {
                // Remove expired or invalid OTP
                otps.remove(mobile);
                log.warn("Invalid or expired OTP for mobile: {}", mobile);
            }
        } else {
            log.warn("No OTP found for mobile: {}", mobile);
        }

        response.put("success", false);
        response.put("status", "ua00");
        response.put("message", "You entered wrong/expired OTP.");
        return response;
    }

    @Override
    @Transactional
    public Map<String, Object> verifySocialLogin(SocialLoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request == null) {
            response.put("success", false);
            response.put("status", "ua14");
            response.put("message", "Invalid request payload.");
            return response;
        }

        String provider = request.getProvider() == null ? "" : request.getProvider().trim().toLowerCase(Locale.ROOT);
        String idToken = request.getIdToken();
        String providerUserId = request.getProviderUserId();

        if (provider.isEmpty() || idToken == null || idToken.trim().isEmpty()) {
            response.put("success", false);
            response.put("status", "ua15");
            response.put("message", "provider and idToken are required.");
            return response;
        }

        String expectedProviderKey;
        if ("google".equals(provider)) {
            expectedProviderKey = "google.com";
        } else if ("apple".equals(provider)) {
            expectedProviderKey = "apple.com";
        } else {
            response.put("success", false);
            response.put("status", "ua16");
            response.put("message", "Unsupported provider.");
            return response;
        }

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
            Object firebaseClaimObj = decodedToken.getClaims().get("firebase");
            if (!(firebaseClaimObj instanceof Map)) {
                response.put("success", false);
                response.put("status", "ua17");
                response.put("message", "Invalid firebase token claim.");
                return response;
            }

            Map<String, Object> firebaseClaim = (Map<String, Object>) firebaseClaimObj;
            String tokenProviderKey = String.valueOf(firebaseClaim.getOrDefault("sign_in_provider", ""));
            if (!expectedProviderKey.equalsIgnoreCase(tokenProviderKey)) {
                response.put("success", false);
                response.put("status", "ua18");
                response.put("message", "Provider does not match token.");
                return response;
            }

            if (providerUserId != null && !providerUserId.trim().isEmpty() && !"null".equalsIgnoreCase(providerUserId)) {
                boolean providerUserIdMatched = false;
                Object identitiesObj = firebaseClaim.get("identities");
                if (identitiesObj instanceof Map) {
                    Object providerIdsObj = ((Map<?, ?>) identitiesObj).get(tokenProviderKey);
                    if (providerIdsObj instanceof List) {
                        for (Object value : (List<?>) providerIdsObj) {
                            if (providerUserId.equals(String.valueOf(value))) {
                                providerUserIdMatched = true;
                                break;
                            }
                        }
                    }
                }

                if (!providerUserIdMatched) {
                    response.put("success", false);
                    response.put("status", "ua19");
                    response.put("message", "providerUserId does not match token.");
                    return response;
                }
            }

            String firebaseUid = decodedToken.getUid();
            String tokenEmail = decodedToken.getEmail();
            Object emailVerifiedClaim = decodedToken.getClaims().get("email_verified");
            boolean emailVerified = emailVerifiedClaim instanceof Boolean && (Boolean) emailVerifiedClaim;
            String displayName = request.getDisplayName();
            if ((displayName == null || displayName.trim().isEmpty() || "null".equalsIgnoreCase(displayName))
                    && decodedToken.getClaims().get("name") != null) {
                displayName = String.valueOf(decodedToken.getClaims().get("name"));
            }
            String picture = decodedToken.getClaims().get("picture") == null
                    ? null
                    : String.valueOf(decodedToken.getClaims().get("picture"));
            String normalizedProviderUserId = providerUserId;
            if (normalizedProviderUserId != null && "null".equalsIgnoreCase(normalizedProviderUserId.trim())) {
                normalizedProviderUserId = null;
            }

            User user = userRepository.findByFirebaseUid(firebaseUid).orElse(null);
            if (user == null && tokenEmail != null && !tokenEmail.trim().isEmpty() && emailVerified) {
                user = userRepository.findByEmail(tokenEmail).orElse(null);
            }

            boolean isNewUser = user == null;
            User saved;
            if (!isNewUser) {
                user.setFirebaseUid(firebaseUid);
                user.setSocialProvider(provider);
                user.setProviderUserId(normalizedProviderUserId);
//                if (displayName != null && !displayName.trim().isEmpty()) {
//                    user.setName(displayName);
//                }
//                if (picture != null && !picture.trim().isEmpty()) {
//                    user.setPicture(picture);
//                }
                saved = userRepository.save(user);
            } else {
                User newUser = new User();
                newUser.setId(setUserId());
                while (userRepository.existsById(newUser.getId())) {
                    newUser.setId(setUserId());
                }
                newUser.setName(displayName);
                newUser.setEmail(tokenEmail);
                newUser.setPicture(picture);
                newUser.setFirebaseUid(firebaseUid);
                newUser.setSocialProvider(provider);
                newUser.setProviderUserId(normalizedProviderUserId);
                newUser.setRegistrationDate(new DateTimeConverter().formatDate());
                ///  写入默认数据
                newUser.setGender("Female");
                newUser.setRegion("PK");
                newUser.setMobile("");
                saved = userRepository.save(newUser);
            }

            UserView uv = userRepository.findOneById(saved.getId()).orElse(null);
            response.put("success", true);
            response.put("status", "ua04");
            response.put("token", jwtTokenUtil.generateToken(saved.getId()));
            response.put("isNewUser", isNewUser);
            response.put("data", toSocialLoginUserResponse(uv));
            response.put("invite", userInviteService.processInvite(saved, isNewUser, request));
            return response;
        } catch (FirebaseAuthException e) {
            log.error("Firebase social login token verify failed. provider={}", provider, e);
            response.put("success", false);
            response.put("status", "ua21");
            response.put("message", "Invalid or expired social token.");
            return response;
        } catch (Exception e) {
            log.error("Social login verify system error. provider={}", provider, e);
            throw new IllegalStateException("Social login processing failed.", e);
        }
    }

    private Map<String, Object> toSocialLoginUserResponse(UserView user) {
        if (user == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("gender", user.getGender());
        result.put("region", user.getRegion());
        result.put("picture", user.getPicture());
        result.put("mobile", user.getMobile());
        result.put("email", user.getEmail());
        return result;
    }

    @Override
    public Boolean authenticateUser(Long userId, String token) {
        return jwtTokenUtil.generateToken(userId).equals(token);
    }

    @Override
    public String getUserToken(Long userId) {
        return jwtTokenUtil.generateToken(userId);
    }

    @Override
    public Map registerUser(User user, MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (userRepository.existsByMobile(user.getMobile())) {
            response.put("success", false);
            response.put("status", "ua03");
            response.put("message", "this mobile number is already registered.");
            return response;
        }
        user.setId(setUserId());
        while (userRepository.existsById(user.getId())) {
            user.setId(setUserId());
        }
        user.setRegistrationDate(new DateTimeConverter().formatDate());

        if (file != null && !file.isEmpty()) {
            if (fileService.store(file, user.getId() + "/")) {
                user.setPicture(user.getId() + "/profile.png");
            }
        }

        UserView uv = userRepository.findOneById(userRepository.save(user).getId()).get();
        response.put("success", true);
        response.put("status", "ua04");
        response.put("token", jwtTokenUtil.generateToken(user.getId()));
        response.put("data", uv);
        return response;
    }

    @Override
    public Map updateUser(Long userId, String userValues, MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        if (userRepository.existsById(userId)) {
            User user = userRepository.findById(userId).get();
            if (!file.isEmpty()) {
                String dir = userId + "/";
                System.out.println(file.getContentType());
                if (!fileService.store(file, dir)) {
                    response.put("success", false);
                    response.put("status", "fe00");
                    response.put("message", "Profile picture can't changed.");
                    return response;
                }
                user.setPicture(user.getId() + "/profile.png");
            }
            user.setName(new Gson().fromJson(userValues, Map.class).getOrDefault("name", user.getName()).toString());
            user.setRegion(
                    new Gson().fromJson(userValues, Map.class).getOrDefault("region", user.getRegion()).toString());
            user.setGender(
                    new Gson().fromJson(userValues, Map.class).getOrDefault("gender", user.getGender()).toString());
            // update by:Ethan date:20240724 desc:If a user has many concact,it will cause
            // infinite loop --start
            List<Contact> userContacts = user.getContacts();
            for (Contact userContact : userContacts) {
                User itemUser = userContact.getUser();
                itemUser.setContacts(null);
            }
            // update by:Ethan date:20240724 desc:If a user has many concact,it will cause
            // infinite loop --end
            UserView uv = userRepository.findOneById(userRepository.save(user).getId()).get();
            response.put("success", true);
            response.put("status", "ua05");
            response.put("data", uv);
            return response;
        }
        response.put("success", false);
        response.put("status", "ua06");
        response.put("message", "unable to update, please try later.");
        return response;

    }

    @Override
    public Boolean deleteUser(long id) {
        try {
            userRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public String deleteAllUsers() {
        userRepository.deleteAll();
        return "All users deleted...";
    }

    @Override
    public Boolean deleteContact(Long userId1, Long userId2) {
        try {
            userRepository.deleteContact(userId1, userId2);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

    @Override
    public UserView addContact(Long requestId, Boolean hideFrom, Boolean hideTheir) {
        Map<String, Object> response = new HashMap<>();
        ContactRequest request = contactRequestRepository.findById(requestId).get();
        System.out.println(request);
        Long userToId = request.getTo().getId();
        Long userFromId = request.getFrom().getId();
        // userRepository.addContact(userToId, userFromId);
        Contact contact = new Contact();
        if (hideTheir || request.isHidemypost()) {
            contact.setHideMyPost(true);
        } else {
            contact.setHideMyPost(false);
        }
        if (hideFrom || request.isHidetheirpost()) {
            contact.setHideTheirPost(true);
        } else {
            contact.setHideTheirPost(false);
        }
        contact.setUser(userRepository.findById(userFromId).get());
        User user = userRepository.findById(userToId).get();
        user.getContacts().add(contact);
        // update by:Ethan date:20240724 desc:If a user has many concact,it will cause
        // infinite loop --start
        List<Contact> userContacts = user.getContacts();
        for (Contact userContact : userContacts) {
            User itemUser = userContact.getUser();
            itemUser.setContacts(null);
        }
        // update by:Ethan date:20240724 desc:If a user has many concact,it will cause
        // infinite loop --end
        userRepository.save(user);
        System.out.println("added");

        // if (request.isHidemypost()) {
        // userRepository.hideMyPost(userToId, userFromId);
        // }
        // if (request.isHidetheirpost()) {
        // userRepository.hideTheirPost(userToId, userFromId);
        // }
        //
        // if (hideFrom) {
        // userRepository.hideMyPost(userFromId, userToId);
        // }
        // if (hideTheir) {
        // userRepository.hideMyPost(userFromId, userToId);
        // }
        contactRequestRepository.deleteById(requestId);
        response.put("success", true);
        response.put("status", "cr06");
        response.put("type", "contact-request-accepted");
        response.put("data", userRepository.findOneById(userToId).get());
        simpTemplate.convertAndSend("/queue/" + userFromId, response);
        return userRepository.findOneById(userFromId).get();
    }

    @Override
    public UserView getUserById(Long userId) {
        return userRepository.findOneById(userId).orElse(null);
    }

    @Override
    public UserView getUserByMobile(String mobile) {
        return userRepository.findOneByMobile(mobile).orElse(null);
    }

    /**
     * added by Ethan
     * 
     * @param countryCode
     * @param identifyId
     * @return
     */
    @Override
    public Map<String, Object> getUserByIdAndMobile(String countryCode, String identifyId) {
        Map<String, Object> result = new HashMap();
        if (!canConvertToInt(identifyId)) {
            return null;
        }
        // 1.search by TalkkiaId
        UserView userView = userRepository.findOneById(Long.valueOf(identifyId)).orElse(null);
        if (userView != null) {
            result.put("userView", userView);
            result.put("from", "ID");
            return result;
        }
        // 2.if can't find user by TalkkiaId,then search by mobile phone number
        // 3.if starts with 0,remove 0,then search by countryCode+mobilePhone(remove the
        // original 0)
        String identifyStr = identifyId.toString();
        // Check if the string representation starts with '0'
        if (identifyStr.startsWith("0")) {
            identifyStr = identifyStr.substring(1); // Remove the first character
        }
        result.put("userView", userRepository.findOneByMobile(countryCode + identifyStr).orElse(null));
        result.put("from", "Mobile");
        // 4.if can't find any users,return null
        return result;
    }

    public boolean canConvertToInt(String str) {
        try {
            Long.valueOf(str);
            // Conversion successful
            return true;
        } catch (Exception e) {
            // Conversion failed
            return false;
        }
    }

    @Override
    public List<Long> getContactsIds(Long userId) {

        List<Long> contactsIds = userRepository.findById(userId).get().getContacts().stream()
                .map(user -> user.getUser().getId()).collect(Collectors.toList());
        contactsIds.addAll(userRepository.findAllByContactsUserId(userId).stream().map(user -> user.getId())
                .collect(Collectors.toList()));
        return contactsIds;
    }

    @Override
    public Boolean contactRequest(Long senderId, Long receiverId, ContactRequest request) {
        Map<String, Object> response = new HashMap<>();
        if (!contactRequestRepository.existsByFromIdAndToId(senderId, receiverId)
                && !contactRequestRepository.existsByFromIdAndToId(receiverId, senderId)) {
            request.setTimestamp(new DateTimeConverter().formatDateTime());
            User sendUser = userRepository.findById(senderId).get();
            sendUser.setContacts(null);
            request.setFrom(sendUser);
            User receiveUser = userRepository.findById(receiverId).get();
            receiveUser.setContacts(null);
            request.setTo(receiveUser);
            long requestId = contactRequestRepository.save(request).getId();
            response.put("success", true);
            response.put("status", "cr02");
            response.put("type", "contact-request");
            response.put("data", contactRequestRepository.findOneById(requestId).get());
            simpTemplate.convertAndSend("/queue/" + receiverId, response);
            return true;
        }
        System.out.println("false");
        return false;
    }

    @Override
    public List<ContactRequestView> getContactRequests(Long userId) {
        List<ContactRequestView> requests = contactRequestRepository.findAllByToId(userId);
        List<ContactRequestView> validRequests = requests.stream()
                .filter(request -> request != null
                        && request.getFrom() != null
                        && request.getFrom().getId() != null)
                .collect(Collectors.toList());
        log.info("getContactRequests userId={}, totalRequests={}, validRequests={}",
                userId, requests.size(), validRequests.size());
        return validRequests;
    }

    @Override
    public List<UserDTO> getAllContacts(Long userId) {
        List<UserDTO> contacts = new ArrayList<>();
        userRepository.findById(userId).get().getContacts().forEach((Contact c) -> {
            User u = c.getUser();
            contacts.add(
                    new UserDTO(u.getId(), u.getName(), u.getGender(), u.getRegion(), u.getPicture(), u.getMobile()));
        });
        contacts.addAll(userRepository.findAllByContactsUserId(userId));
        return contacts;
    }

    @Override
    public ContactRequest getContactRequest(Long requestId) {
        return contactRequestRepository.findById(requestId).get();
    }

    private Long setUserId() {
        return ThreadLocalRandom.current().nextLong(1000000000, 9999999999L);
    }
}
