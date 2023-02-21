package com.techtoids.nota.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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

    public static String getDaysDue(Date date) {
        long dateBeforeInMs = date.getTime();
        long dateAfterInMs = new Date().getTime();

        long timeDiff = Math.abs(dateAfterInMs - dateBeforeInMs);

        long daysDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);
        if (daysDiff > 0) {
            return daysDiff + " Days to due";
        }
        long hourDiff = TimeUnit.HOURS.convert(timeDiff, TimeUnit.MILLISECONDS);
        if (hourDiff > 0) {
            return hourDiff + " Hours to due";
        }
        long minuteDiff = TimeUnit.MINUTES.convert(timeDiff, TimeUnit.MILLISECONDS);
        if (minuteDiff > 0) {
            return minuteDiff + " Mins to due";
        }
        return "Due soon";

    }
}
