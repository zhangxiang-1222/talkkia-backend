package com.talkkia.api.config;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

//to convert and match date and time with user interface requirement.
public class DateTimeConverter {

    Format formatDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    Format formatDate = new SimpleDateFormat("dd/MM/yyyy");
    Format formatTime = new SimpleDateFormat("HH:mm:ss");
    
    public String convertDateTime(long time) {
        Date date = new Date(time);
        return formatDateTime.format(date);
    }
    
    public String formatDateTime(){
        return formatDateTime.format(new Date());
    }
    
    public String formatDate(){
        return formatDate.format(new Date());
    }
    
    public String formatTime(){
        return formatTime.format(new Date());
    }

}
