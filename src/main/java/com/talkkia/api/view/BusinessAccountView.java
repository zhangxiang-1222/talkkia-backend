package com.talkkia.api.view;

//projecting business account data
public interface BusinessAccountView {

        Long getId();
        String getTitle();
        String getContact();
        String getEmail();
        String getWebsite();
        String getAddress();
        String getProfilePicture();
        Boolean getVerified();
}
