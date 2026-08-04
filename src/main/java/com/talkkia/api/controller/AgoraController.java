package com.talkkia.api.controller;

import io.agora.media.RtcTokenBuilder2;
import io.agora.media.RtcTokenBuilder2.Role;

public class AgoraController {
    // Need to set environment variable AGORA_APP_ID
    static String appId = System.getenv("61d7d65abace47d9923bbe316fadda80");
    // Need to set environment variable AGORA_APP_CERTIFICATE
    static String appCertificate = System.getenv("1363cef618ad409f8cb439054ca1870f");

    static String channelName = "7d72365eb983485397e3e3f9d460bdda";
    static String account = "2082341273";
    static int uid = 2082341273;
    static int tokenExpirationInSeconds = 3600;
    static int privilegeExpirationInSeconds = 3600;
    static int joinChannelPrivilegeExpireInSeconds = 3600;
    static int pubAudioPrivilegeExpireInSeconds = 3600;
    static int pubVideoPrivilegeExpireInSeconds = 3600;
    static int pubDataStreamPrivilegeExpireInSeconds = 3600;

    public static void main(String[] args) {
        System.out.printf("App Id: %s\n", appId);
        System.out.printf("App Certificate: %s\n", appCertificate);
        if (appId == null || appId.isEmpty() || appCertificate == null || appCertificate.isEmpty()) {
            System.out.printf("Need to set environment variable AGORA_APP_ID and AGORA_APP_CERTIFICATE\n");
            return;
        }

        RtcTokenBuilder2 token = new RtcTokenBuilder2();
        String result = token.buildTokenWithUid(appId, appCertificate, channelName, uid, tokenExpirationInSeconds, joinChannelPrivilegeExpireInSeconds,
                pubAudioPrivilegeExpireInSeconds, pubVideoPrivilegeExpireInSeconds, pubDataStreamPrivilegeExpireInSeconds);
        System.out.printf("Token with uid and privilege: %s\n", result);

    }
}


