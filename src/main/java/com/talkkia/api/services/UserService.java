package com.talkkia.api.services;

import com.talkkia.api.entity.ChatMessage;
import com.talkkia.api.entity.ContactRequest;
import com.talkkia.api.entity.User;
import com.talkkia.api.model.SocialLoginRequest;
import com.talkkia.api.view.CompleteUserView;
import com.talkkia.api.view.ContactRequestView;
import com.talkkia.api.view.UserDTO;
import java.util.List;
import com.talkkia.api.view.UserView;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author noor
 */
public interface UserService {
    
    public List<UserView> getUsers();
    
    public String signIn(String mobile);
    
    public Boolean signUp(String mobile,int otp, String email);
    
    public Map verifyOTP(String mobile, String email, int otp);
    
    public Boolean authenticateUser(Long userId, String token);
    
    public String getUserToken(Long userId);
    
    public Map registerUser(User user, MultipartFile file);
    
    public Map updateUser(Long userId, String userValues, MultipartFile file);
    
    public Boolean deleteUser(long id);
    
    public UserView addContact(Long requestId, Boolean hideFrom, Boolean hideTheir);
 
    public Boolean deleteContact(Long myId, Long contactId);

    public User getUser(Long userId);
    
    public UserView getUserById(Long userId);
    
    public UserView getUserByMobile(String mobile);

    public Map<String,Object> getUserByIdAndMobile(String countryCode,String identifyId);
    
    public List<Long> getContactsIds(Long userId);

    public Boolean contactRequest(Long senderId, Long receiverId, ContactRequest request);

    public List<ContactRequestView> getContactRequests(Long userId);
    
    public ContactRequest getContactRequest(Long requestId);
    
    public List<UserDTO> getAllContacts(Long userId);

    public String deleteAllUsers();

    public Map<String, Object> verifySocialLogin(SocialLoginRequest request);
}
