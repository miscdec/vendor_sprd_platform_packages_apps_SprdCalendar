package com.android.calendar;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Trace;
import android.widget.Toast;
import android.view.KeyEvent;

/* UNISOC: Modify for bug1076923 {@ */
import android.content.DialogInterface;
import android.app.AlertDialog;
/* @ }*/

/**
 * Activity that asks the user for all {@link #getDesiredPermissions} if any of
 * {@link #getRequiredPermissions} are missing.
 *
 * NOTE: As a result of b/22095159, this can behave oddly in the case where the
 * final permission you are requesting causes an application restart.
 */
public abstract class RequestPermissionsActivityBase extends Activity {
    public static final String PREVIOUS_ACTIVITY_INTENT = "previous_intent";
    private static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;
    private boolean mResumed = false;
    /**
     * @return list of permissions that are needed in order for
     *         {@link #PREVIOUS_ACTIVITY_INTENT} to operate. You only need to
     *         return a single permission per permission group you care about.
     */
    protected abstract String[] getRequiredPermissions();

    /**
     * @return list of permissions that would be useful for
     *         {@link #PREVIOUS_ACTIVITY_INTENT} to operate. You only need to
     *         return a single permission per permission group you care about.
     */
    protected abstract String[] getDesiredPermissions();

    private Intent mPreviousActivityIntent;
    public static Activity mActivity;
    private AlertDialog mErrorPermissionsDialog;  // UNISOC: Modify for bug1076923, 1206642

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreviousActivityIntent = (Intent) getIntent().getExtras().get(PREVIOUS_ACTIVITY_INTENT);

        // Only start a requestPermissions() flow when first starting this
        // activity the first time.
        // The process is likely to be restarted during the permission flow
        // (necessary to enable
        // permissions) so this is important to track.
        if (savedInstanceState == null) {
            requestPermissions();
        }
    }

    /**
     * If any permissions the Note app needs are missing, open an Activity
     * to prompt the user for these permissions. Moreover, finish the current
     * activity.
     *
     * This is designed to be called inside
     * {@link Activity#onCreate}
     */
    protected static boolean startPermissionActivity(Activity activity, String[] requiredPermissions,
            Class<?> newActivityClass) {
        if (!RequestPermissionsActivity.hasPermissions(activity, requiredPermissions)) {
            final Intent intent = new Intent(activity, newActivityClass);
            intent.putExtra(PREVIOUS_ACTIVITY_INTENT, activity.getIntent());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            activity.startActivity(intent);
            /* SPRD: Modify for bug 785521 @{ */
            mActivity = activity;
            if(!(activity.getIntent()).toString().contains("OpenVcalendar")){
                activity.finish();
            }
            /* @} */
            return true;
        }

        return false;
    }

    private static final int attachment_code = 0;

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 0) {
            return;
        }
        if (permissions != null && permissions.length > 0 && isAllGranted(permissions, grantResults)) {
            mPreviousActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(mPreviousActivityIntent);
            /* UNISOC: Modify for bug1166677, update the widget after getting all permission @{ */
            final Intent updateIntent = new Intent(Utils.getWidgetUpdateAction(this));
            updateIntent.setClassName("com.android.calendar",
                    com.android.calendar.widget.CalendarAppWidgetProvider.class.getName());
            sendBroadcast(updateIntent);
            /* @} */
            finish();
            overridePendingTransition(0, 0);
        } else {
            /* UNISOC: Modify for bug1076923,785521,1206642,1404857 @{ */
            //Toast.makeText(this, R.string.error_permissions, Toast.LENGTH_SHORT).show();
            mErrorPermissionsDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.error_permissions)
                    .setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                finish();
                                return true;
                            }
                            return false;
                        }
                    })
                    .setNegativeButton(R.string.refocus_quit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which){
                             finish();
                        }
                    }).setCancelable(false).create();
            mErrorPermissionsDialog.show();
            if(mActivity != null && (mActivity.getIntent()).toString().contains("OpenVcalendar") && !mActivity.isDestroyed()){
                mActivity.finish();
            }
            /* @} */
        }
    }

    private boolean isAllGranted(String permissions[], int[] grantResult) {
        for (int i = 0; i < permissions.length; i++) {
            if (grantResult[i] != PackageManager.PERMISSION_GRANTED && isPermissionRequired(permissions[i])) {
                return false;
            }
        }
        return true;
    }

    private boolean isPermissionRequired(String p) {
        return Arrays.asList(getRequiredPermissions()).contains(p);
    }

    private void requestPermissions() {
        Trace.beginSection("requestPermissions");
        try {
            // Construct a list of missing permissions
            final ArrayList<String> unsatisfiedPermissions = new ArrayList<>();
            for (String permission : getDesiredPermissions()) {
                if (checkSelfPermission(permission)
                        != PackageManager.PERMISSION_GRANTED) {
                    unsatisfiedPermissions.add(permission);
                }
            }
            if (unsatisfiedPermissions.size() == 0) {
                finish();
                return;
            }
            requestPermissions(
                    unsatisfiedPermissions.toArray(new String[unsatisfiedPermissions.size()]),
                    PERMISSIONS_REQUEST_ALL_PERMISSIONS);
        } finally {
            Trace.endSection();
        }
    }

    /*UNISOC: Add for bug 1206642 @{*/
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mErrorPermissionsDialog != null && mErrorPermissionsDialog.isShowing()) {
            mErrorPermissionsDialog.dismiss();
            mErrorPermissionsDialog = null;
        }
    }
    /* @} */

    @Override
    protected void onResume() {
        super.onResume();
        mResumed = true;
    }
    protected static boolean hasPermissions(Context context, String[] permissions) {
        Trace.beginSection("hasPermission");
        try {
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } finally {
            Trace.endSection();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.mResumed) {
            super.onBackPressed();
        }
    }
}
