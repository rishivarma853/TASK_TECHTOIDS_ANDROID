package com.techtoids.nota.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Helper {
    public static Date getUTCDate() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        String utcDate = dateFormat.format(new Date());
        Date dateToReturn = new Date();
        try {
            dateToReturn = (Date) dateFormat.parse(utcDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return dateToReturn;
    }
}
