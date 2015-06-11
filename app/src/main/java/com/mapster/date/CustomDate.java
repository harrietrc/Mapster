package com.mapster.date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by tommyngo on 11/06/15.
 */
public class CustomDate {
    private DateTimeFormatter formatDateTime ;
    private DateTime date;
    private DateTimeFormatter formatDate = DateTimeFormat.forPattern("dd/MM/yyyy");
    private DateTimeFormatter formatTime = DateTimeFormat.forPattern("HH:mm");

    public CustomDate (String dateTime, String format){
        this.formatDateTime = DateTimeFormat.forPattern(format);
        date = this.formatDateTime.parseDateTime(dateTime);
    }

    public CustomDate(String dateTime){
        formatDateTime = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");
        date = formatDateTime.parseDateTime(dateTime);
    }

    public DateTime getDateTime(){
        return date;
    }

    public void addSeconds(int seconds){
        date.plusSeconds(seconds);
    }

    public void addMinutes(int minutes){
        date.plusMinutes(minutes);
    }

    public void addHours(int hours){
        date.plusHours(hours);
    }

    public void addDays(int days){
        date.plusDays(days);
    }

    public void addMonths(int months){
        date.plusMonths(months);
    }

    public String toDateString(){
        return formatDate.print(date);
    }

    public String toTimeString(){
        return formatTime.print(date);
    }

    public String toString(){
        return formatDateTime.print(date);
    }

    public String toDateTimeString(){
        return date.toString();
    }
}
