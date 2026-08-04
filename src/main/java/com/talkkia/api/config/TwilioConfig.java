package com.talkkia.api.config;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TwilioConfig {

    @Value("${twilio.account-sid}")
    private String TWILIO_ACCOUNT_SID;
    @Value("${twilio.auth-token}")
    private String TWILIO_AUTH_TOKEN;
    @Value("${twilio.phone-no}")
    private String TWILIO_PHONE_NO;

    public boolean sendOtp(String mobile, String message) {
        System.out.println(TWILIO_ACCOUNT_SID);
        try {
            Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
            Message msg = Message.creator(new PhoneNumber(mobile), new PhoneNumber(TWILIO_PHONE_NO), message).create();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }

}
