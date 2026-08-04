package com.talkkia.api.view;

//list of steps taken by user's contacts
public interface StepView {

        Integer getCount();

        SteptakerId getSteptaker();

        interface SteptakerId {

            Long getId();
        }
}
