package com.android.calendar;

import android.Manifest.permission;
import android.app.Activity;

public class RequestPermissionsActivity extends RequestPermissionsActivityBase {

    /* UNISOC: Modify for bug1144954,1208688 @{ */
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            permission.READ_CALENDAR,
            permission.WRITE_CALENDAR,
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.READ_CONTACTS,
            permission.READ_SMS
    };
    /* @} */

    @Override
    protected String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    /* UNISOC: Modify for bug1144954,1208688 @{ */
    @Override
    protected String[] getDesiredPermissions() {
        return new String[] {
            permission.READ_CALENDAR,
            permission.WRITE_CALENDAR,
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.READ_CONTACTS,
            permission.READ_SMS
        };
    }
    /* @} */

    public static boolean startPermissionActivity(Activity activity) {
        return startPermissionActivity(activity, REQUIRED_PERMISSIONS,
                RequestPermissionsActivity.class);
    }
}

