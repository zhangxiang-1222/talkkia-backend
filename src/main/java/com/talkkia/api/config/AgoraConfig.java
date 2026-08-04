package com.talkkia.api.config;

import io.agora.media.RtcTokenBuilder;
import io.agora.media.RtcTokenBuilder2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.persistence.criteria.CriteriaBuilder;

/**
 *
 * @author noor
 */
@Configuration
public class AgoraConfig {
    
    @Value("${agora.app-id}")
    private String appId;
    @Value("${agora.app-certificate}")
    private String appCertificate;
    @Value("${agora.token-expiration}")
    private int tokenExpirationInSeconds;
    @Value("${agora.privilege-expiration}")
    private int privilegeExpirationInSeconds;

    @Value("${agora.joinChannelPrivilegeExpireInSeconds}")
    private int joinChannelPrivilegeExpireInSeconds;
    @Value("${agora.pubAudioPrivilegeExpireInSeconds}")
    private int pubAudioPrivilegeExpireInSeconds;
    @Value("${agora.pubVideoPrivilegeExpireInSeconds}")
    private int pubVideoPrivilegeExpireInSeconds;
    @Value("${agora.pubDataStreamPrivilegeExpireInSeconds}")
    private int pubDataStreamPrivilegeExpireInSeconds;


    /*
    public String buildTokenWithUid(String appId,
                                    String appCertificate,
                                    String channelName,
                                    int uid,
                                    int tokenExpire,
                                    int joinChannelPrivilegeExpire,
                                    int pubAudioPrivilegeExpire,
                                    int pubVideoPrivilegeExpire,
                                    int pubDataStreamPrivilegeExpire) {
    */
        public String getAgoraToken(Long userId, String channelName) {
        RtcTokenBuilder2 token = new RtcTokenBuilder2();
        String result = token.buildTokenWithUid(
                appId,
                appCertificate,
                channelName,
                // Integer.parseInt(String.valueOf(ll));
                Integer.parseInt(String.valueOf(userId)),
                tokenExpirationInSeconds,
                joinChannelPrivilegeExpireInSeconds,
                pubAudioPrivilegeExpireInSeconds,
                pubVideoPrivilegeExpireInSeconds,
                pubDataStreamPrivilegeExpireInSeconds);
        System.out.printf("Token with uid and privilege: %s\n", result);
        return result;
    }
}
