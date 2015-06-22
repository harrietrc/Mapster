package com.mapster.date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by tommyngo on 11/06/15.
 */
public class CustomDate {
    private DateTimeFormatter _formatDateTime = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm");;
    private DateTime _date;
    private DateTime _date1970 = new DateTime(1970, 1, 1, 0, 0, DateTimeZone.UTC);
    private DateTimeFormatter _formatDate = DateTimeFormat.forPattern("dd/MM/yyyy");
    private DateTimeFormatter _formatTime = DateTimeFormat.forPattern("HH:mm");

    public CustomDate (String dateTime, String format){
        this._formatDateTime = DateTimeFormat.forPattern(format);
        _date = this._formatDateTime.parseDateTime(dateTime);
    }

    public CustomDate(String dateTime){
        _date = _formatDateTime.parseDateTime(dateTime);
    }

    public CustomDate(long seconds){
        _date = new DateTime(seconds * 1000);
    }

    public DateTime getDateTime(){
        return _date;
    }

    public void addSeconds(int seconds){
        _date = _date.plusSeconds(seconds);
    }

    public void addMinutes(int minutes){
        _date = _date.plusMinutes(minutes);
    }

    public void addHours(int hours){
        _date = _date.plusHours(hours);
    }

    public void addDays(int days){
        _date = _date.plusDays(days);
    }

    public void addMonths(int months){
        _date = _date.plusMonths(months);
    }

    public String toDateString(){
        return _formatDate.print(_date);
    }

    public String toTimeString(){
        return _formatTime.print(_date);
    }

    public String toString(){
        return _formatDateTime.print(_date);
    }

    public String toDateTimeString(){
        return _date.toString();
    }

    public int secondsBetween(){
        return Math.abs(Seconds.secondsBetween(_date.toDateTime(DateTimeZone.UTC), _date1970).getSeconds());
    }

    public int secondsBetween(DateTime d){
        return Math.abs(Seconds.secondsBetween(d.toDateTime(DateTimeZone.UTC), _date1970).getSeconds());
    }
}
