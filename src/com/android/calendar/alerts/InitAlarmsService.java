/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.calendar.alerts;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.android.calendar.R;
import com.android.calendar.Utils;

/**
 * Service for clearing all scheduled alerts from the CalendarAlerts table and
 * rescheduling them.  This is expected to be called only on boot up, to restore
 * the AlarmManager alarms that were lost on device restart.
 */
public class InitAlarmsService extends IntentService {
    private static final String TAG = "InitAlarmsService";
    public static int NOTIFICATION_ID = 0x10000000; // UNISOC: Modify for bug1146368,1170619
    private static final String SCHEDULE_ALARM_REMOVE_PATH = "schedule_alarms_remove";
    private static final Uri SCHEDULE_ALARM_REMOVE_URI = Uri.withAppendedPath(
            CalendarContract.CONTENT_URI, SCHEDULE_ALARM_REMOVE_PATH);

    // Delay for rescheduling the alarms must be great enough to minimize race
    // conditions with the provider's boot up actions.
    private static final long DELAY_MS = 30000;

    public InitAlarmsService() {
        super("InitAlarmsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        /* UNISOC: Modify for bug1147235, 1178708，1146388，1169284，1188866  {@ */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final NotificationManager notificationMgr = this.getSystemService(NotificationManager.class);
            final NotificationChannel nchannel = new NotificationChannel(Utils.SERVICE_NOTIFICATION_CHANNEL,
                    this.getString(R.string.app_label),NotificationManager.IMPORTANCE_LOW);
            nchannel.setShowBadge(false);
            notificationMgr.createNotificationChannel(nchannel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
            builder.setChannelId(Utils.SERVICE_NOTIFICATION_CHANNEL);
            builder.setGroup(Utils.SERVICE_NOTIFICATION_GROUP);
            builder.setSmallIcon(R.drawable.service_notification_icon);
            builder.setContentTitle(this.getString(R.string.app_label));
            startForeground(NOTIFICATION_ID, builder.build());
        }
        /* @} */
        // Delay to avoid race condition of in-progress alarm scheduling in provider.
        SystemClock.sleep(DELAY_MS);
        Log.d(TAG, "Clearing and rescheduling alarms.");
        try {
            getContentResolver().update(SCHEDULE_ALARM_REMOVE_URI, new ContentValues(), null,
                    null);
        } catch (java.lang.IllegalArgumentException e) {
            // java.lang.IllegalArgumentException:
            //     Unknown URI content://com.android.calendar/schedule_alarms_remove

            // Until b/7742576 is resolved, just catch the exception so the app won't crash
            Log.e(TAG, "update failed: " + e.toString());
        }
    }
}
