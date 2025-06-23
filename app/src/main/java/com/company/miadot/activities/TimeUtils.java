package com.company.miadot.activities;

import android.text.format.DateUtils;

public class TimeUtils {
    public static String getTimeAgo(long timeMillis) {
        return DateUtils.getRelativeTimeSpanString(
                timeMillis,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
        ).toString();
    }
}
