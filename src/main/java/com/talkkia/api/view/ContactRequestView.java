package com.talkkia.api.view;

//contacts request
public interface ContactRequestView {

    Long getId();

    String getTimestamp();

    String getGreeting();

    FromId getFrom();

    interface FromId {

        Long getId();

        String getName();

        String getPicture();
    }

}
