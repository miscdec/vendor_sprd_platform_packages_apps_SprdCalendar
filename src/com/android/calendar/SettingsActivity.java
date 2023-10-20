package com.android.calendar;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.provider.CalendarContract.Calendars;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.provider.CalendarContract;
import android.provider.CalendarContract.CalendarCache;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.text.format.Time;
import android.widget.ImageView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.view.View;

import com.android.calendar.alerts.AlertReceiver;
import com.android.timezonepicker.TimeZoneInfo;
import com.android.timezonepicker.TimeZonePickerDialog;
import com.android.timezonepicker.TimeZonePickerDialog.OnTimeZoneSetListener;
import com.android.timezonepicker.TimeZonePickerUtils;
import android.provider.Settings;
//import android.provider.SettingsEx;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class SettingsActivity extends Activity {

    static final String SHARED_PREFS_NAME = "com.android.calendar_preferences";
    static final String SHARED_PREFS_NAME_NO_BACKUP = "com.android.calendar_preferences_no_backup";

    private static final String FRAG_TAG_TIME_ZONE_PICKER = "TimeZonePicker";

    // Preference keys
    public static final String KEY_HIDE_DECLINED = "preferences_hide_declined";
    public static final String KEY_WEEK_START_DAY = "preferences_week_start_day";
    public static final String KEY_SHOW_WEEK_NUM = "preferences_show_week_num";
    public static final String KEY_DAYS_PER_WEEK = "preferences_days_per_week";
    public static final String KEY_SKIP_SETUP = "preferences_skip_setup";

    public static final String KEY_CLEAR_SEARCH_HISTORY = "preferences_clear_search_history";

    public static final String KEY_ALERTS_CATEGORY = "preferences_alerts_category";
    public static final String KEY_ALERTS = "preferences_alerts";
    public static final String KEY_ALERTS_VIBRATE = "preferences_alerts_vibrate";
    public static final String KEY_ALERTS_RINGTONE = "preferences_alerts_ringtone";
    public static final String KEY_ALERTS_POPUP = "preferences_alerts_popup";

    public static final String KEY_SHOW_CONTROLS = "preferences_show_controls";

    public static final String KEY_DEFAULT_REMINDER = "preferences_default_reminder";
    public static final int NO_REMINDER = -1;
    public static final String NO_REMINDER_STRING = "-1";
    public static final int REMINDER_DEFAULT_TIME = 10; // in minutes

    public static final String KEY_DEFAULT_CELL_HEIGHT = "preferences_default_cell_height";
    public static final String KEY_VERSION = "preferences_version";

    /** Key to SharePreference for default view (CalendarController.ViewType) */
    public static final String KEY_START_VIEW = "preferred_startView";
    /**
     * Key to SharePreference for default detail view
     * (CalendarController.ViewType) Typically used by widget
     */
    public static final String KEY_DETAILED_VIEW = "preferred_detailedView";
    public static final String KEY_DEFAULT_CALENDAR = "preference_defaultCalendar";

    // These must be in sync with the array preferences_week_start_day_values
    public static final String WEEK_START_DEFAULT = "-1";
    public static final String WEEK_START_SATURDAY = "7";
    public static final String WEEK_START_SUNDAY = "1";
    public static final String WEEK_START_MONDAY = "2";

    // These keys are kept to enable migrating users from previous versions
    private static final String KEY_ALERTS_TYPE = "preferences_alerts_type";
    private static final String ALERT_TYPE_ALERTS = "0";
    private static final String ALERT_TYPE_STATUS_BAR = "1";
    private static final String ALERT_TYPE_OFF = "2";
    static final String KEY_HOME_TZ_ENABLED = "preferences_home_tz_enabled";
    static final String KEY_HOME_TZ = "preferences_home_tz";

    // Default preference values
    // SPRD: Add for bug547211, change default view to month view.
    public static final int DEFAULT_START_VIEW = CalendarController.ViewType.MONTH;
    public static final int DEFAULT_DETAILED_VIEW = CalendarController.ViewType.DAY;
    public static final boolean DEFAULT_SHOW_WEEK_NUM = false;
    // This should match the XML file.
    public static final String DEFAULT_RINGTONE = "content://settings/system/notification_sound";
    private ActionBar mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferences())
                .commit();
        mActionBar = getActionBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* UNISOC: Modify for bug1224502 {@ */
        if (mActionBar != null) {
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP,
                    ActionBar.DISPLAY_HOME_AS_UP);
//            mActionBar.setHomeAsUpIndicator(R.drawable.ic_ab_back_holo_light);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_jing_back_black);
        }
        /* }@ */
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class GeneralPreferences extends PreferenceFragment implements
            OnSharedPreferenceChangeListener, OnPreferenceChangeListener,
            OnTimeZoneSetListener {

        CheckBoxPreference mAlert;
        CheckBoxPreference mVibrate;
        RingtonePreference mRingtone;
        CheckBoxPreference mPopup;
        CheckBoxPreference mUseHomeTZ;
        CheckBoxPreference mHideDeclined;
        Preference mHomeTZ;
        TimeZonePickerUtils mTzPickerUtils;
        ListPreference mWeekStart;
        ListPreference mDefaultReminder;

        private String mTimeZoneId;
        // SPRD: for bug 441656 , change the start day of week, the time can't
        // be located to the current time
        private Editor startWeekEdit;

        /** Return a properly configured SharedPreferences instance */
        public static SharedPreferences getSharedPreferences(Context context) {
            return context.getSharedPreferences(SHARED_PREFS_NAME,
                    Context.MODE_PRIVATE);
        }

        /** Set the default shared preferences in the proper context */
        public static void setDefaultValues(Context context) {
            PreferenceManager.setDefaultValues(context, SHARED_PREFS_NAME,
                    Context.MODE_PRIVATE, R.xml.general_preferences, false);
        }

        // SPRD: bug 623968, request runtime permissions
        public static final String ACTIVITY_FLAG = "Activity_Flag";

        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);

            final Activity activity = getActivity();

            /* SPRD: bug 741231, After canceling the fixed screen, click on the calendar is also the white screen {@ */
            if (RequestPermissionsActivity.startPermissionActivity(activity)) {
                return;
            }
            /* @} */

            // Make sure to always use the same preferences file regardless of
            // the package name
            // we're running under
            final PreferenceManager preferenceManager = getPreferenceManager();
            final SharedPreferences sharedPreferences = getSharedPreferences(activity);
            preferenceManager.setSharedPreferencesName(SHARED_PREFS_NAME);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.general_preferences);

            final PreferenceScreen preferenceScreen = getPreferenceScreen();
            mAlert = (CheckBoxPreference) preferenceScreen
                    .findPreference(KEY_ALERTS);
            mVibrate = (CheckBoxPreference) preferenceScreen
                    .findPreference(KEY_ALERTS_VIBRATE);
            Vibrator vibrator = (Vibrator) activity
                    .getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator == null || !vibrator.hasVibrator()) {
                PreferenceCategory mAlertGroup = (PreferenceCategory) preferenceScreen
                        .findPreference(KEY_ALERTS_CATEGORY);
                mAlertGroup.removePreference(mVibrate);
            }

            mRingtone = (RingtonePreference) preferenceScreen
                    .findPreference(KEY_ALERTS_RINGTONE);
            String ringToneUri = Utils.getRingTonePreference(activity);

            // Set the ringToneUri to the backup-able shared pref only so that
            // the Ringtone dialog will open up with the correct value.
            /* UNISOC: Modify for bug1223288 {@ */
            if (ringToneUri != null) {
                final Editor editor = preferenceScreen.getEditor();
                if (editor != null) {
                    editor.putString(KEY_ALERTS_RINGTONE, ringToneUri).apply();

                    String ringtoneDisplayString = getRingtoneTitleFromUri(activity,
                            ringToneUri);
                    mRingtone.setSummary(ringtoneDisplayString == null ? ""
                            : ringtoneDisplayString);
                }
            }
            /* }@ */

            mPopup = (CheckBoxPreference) preferenceScreen
                    .findPreference(KEY_ALERTS_POPUP);
            mUseHomeTZ = (CheckBoxPreference) preferenceScreen
                    .findPreference(KEY_HOME_TZ_ENABLED);
            mHideDeclined = (CheckBoxPreference) preferenceScreen
                    .findPreference(KEY_HIDE_DECLINED);
            mWeekStart = (ListPreference) preferenceScreen
                    .findPreference(KEY_WEEK_START_DAY);
            mDefaultReminder = (ListPreference) preferenceScreen
                    .findPreference(KEY_DEFAULT_REMINDER);
            mHomeTZ = preferenceScreen.findPreference(KEY_HOME_TZ);
            mWeekStart.setSummary(mWeekStart.getEntry());
            mDefaultReminder.setSummary(mDefaultReminder.getEntry());

            // This triggers an asynchronous call to the provider to refresh the
            // data in shared pref
            mTimeZoneId = Utils.getTimeZone(activity, null);

            SharedPreferences prefs = CalendarUtils.getSharedPreferences(
                    activity, Utils.SHARED_PREFS_NAME);
            // SPRD: for bug 441656 , change the start day of week, the time
            // can't be located to the current time
            startWeekEdit = prefs.edit();
            ((PreferenceGroup) findPreference("preferences_general"))
                    .removePreference(findPreference("preferences_hide_declined"));
            ((PreferenceGroup) findPreference("preferences_general"))
                    .removePreference(findPreference("preferences_show_week_num"));
            ((PreferenceGroup) findPreference("preferences_general"))
                    .removePreference(findPreference("preferences_clear_search_history"));
            ((PreferenceGroup) findPreference("preferences_alerts_category"))
                    .removePreference(findPreference("preferences_quick_responses"));
            /* UNISOC: Modify for bug1127974 @{ */
            //((PreferenceGroup) findPreference("preferences_alerts_category"))
            //        .removePreference(findPreference("preferences_alerts"));
            /* @} */
            ((PreferenceGroup) findPreference("preferences_alerts_category"))
                    .removePreference(findPreference("preferences_alerts_ringtone"));
            ((PreferenceGroup) findPreference("preferences_alerts_category"))
                    .removePreference(findPreference("preferences_alerts_vibrate"));
            ((PreferenceGroup) findPreference("preferences_alerts_category"))
                    .removePreference(findPreference("preferences_alerts_popup"));

            // Utils.getTimeZone will return the currentTimeZone instead of the
            // one
            // in the shared_pref if home time zone is disabled. So if home tz
            // is
            // off, we will explicitly read it.
            if (!prefs.getBoolean(KEY_HOME_TZ_ENABLED, false)) {
                mTimeZoneId = prefs.getString(KEY_HOME_TZ,
                        Time.getCurrentTimezone());
            }

            mHomeTZ.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showTimezoneDialog();
                    return true;
                }
            });

            if (mTzPickerUtils == null) {
                mTzPickerUtils = new TimeZonePickerUtils(getActivity());
            }
            CharSequence timezoneName = mTzPickerUtils.getGmtDisplayName(
                    getActivity(), mTimeZoneId, System.currentTimeMillis(),
                    false);
            mHomeTZ.setSummary(timezoneName != null ? timezoneName
                    : mTimeZoneId);

            TimeZonePickerDialog tzpd = (TimeZonePickerDialog) activity
                    .getFragmentManager().findFragmentByTag(
                            FRAG_TAG_TIME_ZONE_PICKER);
            if (tzpd != null) {
                tzpd.setOnTimeZoneSetListener(this);
            }

            migrateOldPreferences(sharedPreferences);

            updateChildPreferences();
            // SPRD: bug387847 2014-12-29 can not chose ringtone by file
            // explorer.
            configPreferenceChangeListeners(true);
        }

        /* SPRD: Bug514347 update the summary of ringtone file deleted */
        @Override
        public void onResume() {
            String ringToneUri = Utils.getRingTonePreference(getActivity());
            if (ringToneUri != null) {
                final Editor editor = getPreferenceScreen().getEditor();
                if (editor != null) {
                    editor.putString(KEY_ALERTS_RINGTONE, ringToneUri).apply();

                    String ringtoneDisplayString = getRingtoneTitleFromUri(
                            getActivity(), ringToneUri);
                    mRingtone.setSummary(ringtoneDisplayString == null ? ""
                            : ringtoneDisplayString);
                }
            }
            super.onResume();
        }

        /* @} */

        private void showTimezoneDialog() {
            final Activity activity = getActivity();
            /*
             * SPRD: Bug 518836 java.lang.IllegalStateException: Can not perform
             * this action after onSaveInstanceState @{
             */
            // remove isResume,the method has been removed by google
            // if (activity == null || !activity.isResumed()) {
            if (activity == null) {
                return;
            }
            /* @} */

            Bundle b = new Bundle();
            b.putLong(TimeZonePickerDialog.BUNDLE_START_TIME_MILLIS,
                    System.currentTimeMillis());
            b.putString(TimeZonePickerDialog.BUNDLE_TIME_ZONE,
                    Utils.getTimeZone(activity, null));

            FragmentManager fm = getActivity().getFragmentManager();
            TimeZonePickerDialog tzpd = (TimeZonePickerDialog) fm
                    .findFragmentByTag(FRAG_TAG_TIME_ZONE_PICKER);
            if (tzpd != null) {
                tzpd.dismissAllowingStateLoss();    //SPRD: Add for bug772567
            }
            tzpd = new TimeZonePickerDialog();
            tzpd.setArguments(b);
            tzpd.setOnTimeZoneSetListener(this);
            tzpd.show(fm, FRAG_TAG_TIME_ZONE_PICKER);    //SPRD: Add for bug772567
        }

        /*
         * SPRD: bug387847 2014-12-29 can not chose ringtone by file explorer.
         * @{
         *
         * @Override public void onStart() { super.onStart();
         * getPreferenceScreen().getSharedPreferences()
         * .registerOnSharedPreferenceChangeListener(this);
         * setPreferenceListeners(this); }
         *
         * @}
         */

        /**
         * Sets up all the preference change listeners to use the specified
         * listener.
         */
        private void setPreferenceListeners(OnPreferenceChangeListener listener) {
            mUseHomeTZ.setOnPreferenceChangeListener(listener);
            mHomeTZ.setOnPreferenceChangeListener(listener);
            mWeekStart.setOnPreferenceChangeListener(listener);
            mDefaultReminder.setOnPreferenceChangeListener(listener);
            mRingtone.setOnPreferenceChangeListener(listener);
            mHideDeclined.setOnPreferenceChangeListener(listener);
            mVibrate.setOnPreferenceChangeListener(listener);
        }

        /*
         * SPRD: bug387847 2014-12-29 can not chose ringtone by file explorer.
         * @{
         *
         * @Override public void onStop() {
         * getPreferenceScreen().getSharedPreferences()
         * .unregisterOnSharedPreferenceChangeListener(this);
         * setPreferenceListeners(null); super.onStop(); }
         *
         * @}
         */

        @Override
        public void onSharedPreferenceChanged(
                SharedPreferences sharedPreferences, String key) {
            Activity a = getActivity();
            if (key.equals(KEY_ALERTS)) {
                updateChildPreferences();
                if (a != null) {
                    Intent intent = new Intent();
                    intent.setClass(a, AlertReceiver.class);
                    if (mAlert.isChecked()) {
                        intent.setAction(AlertReceiver.ACTION_DISMISS_OLD_REMINDERS);
                    } else {
                        intent.setAction(AlertReceiver.EVENT_REMINDER_APP_ACTION);
                    }
                    a.sendBroadcast(intent);
                }
            }
            if (a != null) {
                BackupManager.dataChanged(a.getPackageName());
            }
        }

        /**
         * Handles time zone preference changes
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String tz;
            final Activity activity = getActivity();
            if (preference == mUseHomeTZ) {
                if ((Boolean) newValue) {
                    tz = mTimeZoneId;
                } else {
                    tz = CalendarCache.TIMEZONE_TYPE_AUTO;
                }
                Utils.setTimeZone(activity, tz);
                return true;
            } else if (preference == mHideDeclined) {
                mHideDeclined.setChecked((Boolean) newValue);
                Intent intent = new Intent(
                        Utils.getWidgetScheduledUpdateAction(activity));
                intent.setDataAndType(CalendarContract.CONTENT_URI,
                        Utils.APPWIDGET_DATA_TYPE);
                intent.setClassName("com.android.calendar",
                        com.android.calendar.widget.CalendarAppWidgetService.class.getName());
                activity.sendBroadcast(intent);
                return true;
            } else if (preference == mWeekStart) {
                /*
                 * SPRD: for bug 441656 , change the start day of week, the time
                 * can't be located to the current time @{
                 */
                startWeekEdit.putBoolean(Utils.WEEK_START_DAY_CHANGE, true);
                startWeekEdit.commit();
                /* @} */
                mWeekStart.setValue((String) newValue);
                mWeekStart.setSummary(mWeekStart.getEntry());
            } else if (preference == mDefaultReminder) {
                mDefaultReminder.setValue((String) newValue);
                mDefaultReminder.setSummary(mDefaultReminder.getEntry());
            } else if (preference == mRingtone) {
                if (newValue instanceof String) {
                    Utils.setRingTonePreference(activity, (String) newValue);
                    String ringtone = getRingtoneTitleFromUri(activity,
                            (String) newValue);
                    mRingtone.setSummary(ringtone == null ? "" : ringtone);
                }
                return true;
            } else if (preference == mVibrate) {
                mVibrate.setChecked((Boolean) newValue);
                return true;
            } else {
                return true;
            }
            return false;
        }

        public String getRingtoneTitleFromUri(Context context, String uri) {
            if (TextUtils.isEmpty(uri)) {
                return null;
            }

            Ringtone ring = RingtoneManager.getRingtone(getActivity(),
                    Uri.parse(uri));

            /*
             * SPRD:ADD Bug397766/466734/472865/514347, invalid ringing
             * reminder. @{
             */
            // SPRD for bug528419 Fail to ring when the song is deleted
            if (ring != null && Utils.hasDeleted(context, Uri.parse(uri))) {
                final Activity activity = getActivity();
                Toast.makeText(
                        activity,
                        getResources().getString(
                                R.string.ringtone_default_message),
                        Toast.LENGTH_SHORT).show();
                /*
                 * SPRD: add for bug532711, java.lang.NullPointerException, use
                 * Uri.parse on a null string. @}
                 */
                if (TextUtils.isEmpty(DEFAULT_RINGTONE)) {
                    return null;
                }
                /* @} */
                if (Utils.hasDeleted(context, Uri.parse(DEFAULT_RINGTONE))) {
                    /*UNISOC: Modified for bug 1055199 @{*/
                    String notifiUriString = Settings.System.getString(
                            context.getContentResolver(),
                            Settings.System.NOTIFICATION_SOUND);
                    /*}@*/
                    /*
                     * SPRD: add for bug532711, java.lang.NullPointerException,
                     * use Uri.parse on a null string. @}
                     */
                    if (TextUtils.isEmpty(notifiUriString)) {
                        return null;
                    }
                    /* @} */
                    RingtoneManager.setActualDefaultRingtoneUri(context,
                            RingtoneManager.TYPE_NOTIFICATION,
                            Uri.parse(notifiUriString));
                }
                Utils.setRingTonePreference(activity, DEFAULT_RINGTONE);
                ring = RingtoneManager.getRingtone(getActivity(),
                        Uri.parse(DEFAULT_RINGTONE));

                // Set the ringToneUri to the backup-able shared pref only so
                // that
                // the Ringtone dialog will open up with the correct value.
                final Editor editor = getPreferenceScreen().getEditor();
                editor.putString(KEY_ALERTS_RINGTONE, DEFAULT_RINGTONE).apply();
                /* @} */
            }
            /* @} */

            if (ring != null) {
                return ring.getTitle(context);
            }
            return null;
        }

        /**
         * If necessary, upgrades previous versions of preferences to the
         * current set of keys and values.
         *
         * @param prefs
         *            the preferences to upgrade
         */
        private void migrateOldPreferences(SharedPreferences prefs) {
            // If needed, migrate vibration setting from a previous version

            mVibrate.setChecked(Utils.getDefaultVibrate(getActivity(), prefs));

            // If needed, migrate the old alerts type settin
            if (!prefs.contains(KEY_ALERTS) && prefs.contains(KEY_ALERTS_TYPE)) {
                String type = prefs.getString(KEY_ALERTS_TYPE,
                        ALERT_TYPE_STATUS_BAR);
                if (type.equals(ALERT_TYPE_OFF)) {
                    mAlert.setChecked(false);
                    mPopup.setChecked(false);
                    mPopup.setEnabled(false);
                } else if (type.equals(ALERT_TYPE_STATUS_BAR)) {
                    mAlert.setChecked(true);
                    mPopup.setChecked(false);
                    mPopup.setEnabled(true);
                } else if (type.equals(ALERT_TYPE_ALERTS)) {
                    mAlert.setChecked(true);
                    mPopup.setChecked(true);
                    mPopup.setEnabled(true);
                }
                // clear out the old setting
                prefs.edit().remove(KEY_ALERTS_TYPE).commit();
            }
        }

        /**
         * Keeps the dependent settings in sync with the parent preference, so
         * for example, when notifications are turned off, we disable the
         * preferences for configuring the exact notification behavior.
         */
        private void updateChildPreferences() {
            if (mAlert.isChecked()) {
                mVibrate.setEnabled(true);
                mRingtone.setEnabled(true);
                mPopup.setEnabled(true);
            } else {
                mVibrate.setEnabled(false);
                mRingtone.setEnabled(false);
                mPopup.setEnabled(false);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            final String key = preference.getKey();
            if (KEY_CLEAR_SEARCH_HISTORY.equals(key)) {
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                        getActivity(), Utils.getSearchAuthority(getActivity()),
                        CalendarRecentSuggestionsProvider.MODE);
                suggestions.clearHistory();
                Toast.makeText(getActivity(), R.string.search_history_cleared,
                        Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return super
                        .onPreferenceTreeClick(preferenceScreen, preference);
            }
        }

        @Override
        public void onTimeZoneSet(TimeZoneInfo tzi) {
            if (mTzPickerUtils == null) {
                mTzPickerUtils = new TimeZonePickerUtils(getActivity());
            }

            final CharSequence timezoneName = mTzPickerUtils
                    .getGmtDisplayName(getActivity(), tzi.mTzId,
                            System.currentTimeMillis(), false);
            mHomeTZ.setSummary(timezoneName);
            Utils.setTimeZone(getActivity(), tzi.mTzId);
        }

        /*
         * SPRD: bug387847 2014-12-29 can not chose ringtone by file explorer.
         * @{
         */
        @Override
        public void onDestroy() {
            configPreferenceChangeListeners(false);
            super.onDestroy();
        }

        private void configPreferenceChangeListeners(boolean isSet) {
            /* SPRD: bug 623968, Request runtime permissions {@ */
            if (getPreferenceScreen() == null) {
                return;
            }
            /* @} */
            if (isSet) {
                getPreferenceScreen().getSharedPreferences()
                        .registerOnSharedPreferenceChangeListener(this);
                setPreferenceListeners(this);
            } else {
                // unset listeners
                getPreferenceScreen().getSharedPreferences()
                        .unregisterOnSharedPreferenceChangeListener(this);
                setPreferenceListeners(null);
            }
        }
    }
    /* SPRD: bug387847 2014-12-29 @} */

}
