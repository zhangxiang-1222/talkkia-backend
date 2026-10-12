package com.talkkia.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {
    private String provider;
    private String providerUserId;
    private String idToken;
    private String authorizationCode;
    private String email;
    private String displayName;
    private String inviteCode;
}
