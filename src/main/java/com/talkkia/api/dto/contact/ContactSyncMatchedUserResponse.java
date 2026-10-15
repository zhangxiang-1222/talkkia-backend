package com.talkkia.api.dto.contact;

public class ContactSyncMatchedUserResponse {
    private Long id;
    private String name;
    private String picture;
    private String syncToken;

    public ContactSyncMatchedUserResponse(Long id,
                                          String name,
                                          String picture,
                                          String syncToken) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.syncToken = syncToken;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public String getSyncToken() {
        return syncToken;
    }

}
