package com.techtoids.nota.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.widget.Toast;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class BasicHelper {

    public static boolean isNotNullOrEmpty(String string) {
        return string != null && string.isEmpty() == false;
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static String getDaysDue(Date date) {
        long dateBeforeInMs = date.getTime();
        long dateAfterInMs = new Date().getTime();

        long timeDiff = dateBeforeInMs - dateAfterInMs;

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
        long millisecondsDiff = TimeUnit.MILLISECONDS.convert(timeDiff, TimeUnit.MILLISECONDS);

        if (millisecondsDiff > 0) {
            return "Due soon";
        }
        return "Overdue";
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        if (networkCapabilities != null) {
            return true;
        } else {
            Toast.makeText(context, "No network detected", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
