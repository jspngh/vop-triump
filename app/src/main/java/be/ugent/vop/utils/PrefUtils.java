package be.ugent.vop.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class PrefUtils {

    /**
     * String value storing the current Foursquare oauth token
     */
    public static final String FOURSQUARE_TOKEN = "FoursquareToken";

    /**
     * String value storing the current backend auth token
     */
    public static final String BACKEND_TOKEN = "backendToken";

    /**
     * String value storing the user id
     */
    public static final String USER_ID = "UserId";

    /**
     * String value storing the user's first name
     */
    public static final String USER_FIRST_NAME = "firstName";

    /**
     * String value storing the user's last name
     */
    public static final String USER_LAST_NAME = "lastName";

    /**
     * String value storing the user's email
     */
    public static final String USER_EMAIL = "email";

    /**
     * String value storing the user's profile picture prefix
     */
    public static final String PROFILE_PIC_PREFIX = "profilePicPrefix";

    /**
     * String value storing the user's profile picture suffix
     */
    public static final String PROFILE_PIC_SUFFIX = "profilePicSuffix";
    /**
     * Boolean value storing whether this is the first launch of the app
     */
    public static final String FIRST_LAUNCH = "firstLaunch";

    public final static String DISPLAY_NAME = "user_display_name";

    public final static String RECEIVE_NOTIFICATIONS = "notifications_new_message";
    public final static String DISABLE_SHARE_PROFILE = "disable_share_user_profile";
    public final static String THEME_DARK = "theme_dark";

    public static String getFoursquareToken(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(FOURSQUARE_TOKEN, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setFoursquareToken(final Context context, String token){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(FOURSQUARE_TOKEN, token).commit();
    }

    public static String getBackendToken(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(BACKEND_TOKEN, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setBackendToken(final Context context, String token){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(BACKEND_TOKEN, token).commit();
    }

    public static String getUserId(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(USER_ID, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setUserId(final Context context, String userId){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(USER_ID, userId).commit();
    }

    public static boolean isFirstLaunch(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(FIRST_LAUNCH, true);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setFirstLaunch(final Context context, boolean firstLaunch){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(FIRST_LAUNCH, firstLaunch).commit();
    }

    @SuppressLint("CommitPrefEdits")
    public static void clearCredentials(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().remove(FOURSQUARE_TOKEN)
                .remove(BACKEND_TOKEN)
                .remove(USER_ID)
                .remove(USER_FIRST_NAME)
                .remove(USER_LAST_NAME)
                .remove(USER_EMAIL).commit();
    }

    public static String getUserFirstName(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(USER_FIRST_NAME, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setUserFirstName(final Context context, String firstName){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(USER_FIRST_NAME, firstName).commit();
    }

    public static String getUserLastName(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(USER_LAST_NAME, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setUserLastName(final Context context, String lastName){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(USER_LAST_NAME, lastName).commit();
    }

    public static String getUserEmail(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(USER_EMAIL, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setUserEmail(final Context context, String email){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(USER_EMAIL, email).commit();
    }

    public static String getProfilePicPrefix(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PROFILE_PIC_PREFIX, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setProfilePicPrefix(final Context context, String prefix){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PROFILE_PIC_PREFIX, prefix).commit();
    }

    public static String getProfilePicSuffix(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(PROFILE_PIC_SUFFIX, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setProfilePicSuffix(final Context context, String suffix){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(PROFILE_PIC_SUFFIX, suffix).commit();
    }

    public static boolean getDarkTheme(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(THEME_DARK, false);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setDarkTheme(final Context context, boolean darkTheme){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(THEME_DARK, darkTheme).commit();
    }

    public static boolean getReceiveNotifications(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(RECEIVE_NOTIFICATIONS, true);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setReceiveNotifications(final Context context, boolean notifications){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(RECEIVE_NOTIFICATIONS, notifications).commit();
    }

    public static boolean getShareProfileDisabled(final Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(DISABLE_SHARE_PROFILE, false);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setShareProfileDisabled(final Context context, boolean share){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(DISABLE_SHARE_PROFILE, share).commit();
    }


    public static void registerOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(final Context context,
                                                                SharedPreferences.OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
