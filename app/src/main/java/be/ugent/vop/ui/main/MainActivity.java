package be.ugent.vop.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.LocationService;
import be.ugent.vop.NetworkController;
import be.ugent.vop.R;
import be.ugent.vop.feedback.Feedback;
import be.ugent.vop.ui.group.GroupListFragment;
import be.ugent.vop.ui.venue.CheckinFragment;
import be.ugent.vop.ui.widget.SlidingTabLayout;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.utils.PrefUtils;

public class MainActivity extends BaseActivity {
    public static final String WELCOME_MSG = "WelcomeMessage";
    // -------------------- GCM ------------------------
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    String SENDER_ID = "795681051514";
    String regid;
    GoogleCloudMessaging gcm;
    Context context;
    //--------------------------------------------------

    private static final String TAG = "MainActivity";

    private LocationService mLocationService;
    private boolean isBound = false;
    public boolean displayWelcome;
    public final Object syncToken = new Object();

    // View pager and adapter (for narrow mode)
    ViewPager mViewPager = null;
    OurViewPagerAdapter mViewPagerAdapter = null;
    SlidingTabLayout mSlidingTabLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);

        super.onCreate(savedInstanceState);
        Bundle args = getIntent().getExtras();
        if(args != null) displayWelcome = args.getBoolean(WELCOME_MSG, false);
        else displayWelcome = false;

        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPagerAdapter = new OurViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        //setSlidingTabLayoutContentDescriptions();
        Resources res = getResources();
        if(darkTheme) mSlidingTabLayout.setBackgroundColor(res.getColor(R.color.theme_dark_primary));
        if(darkTheme) mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.theme_dark_accent));
        else mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        //GCM
        context = getApplicationContext();
        if (checkPlayServices()) {
            Log.d(TAG,"checkPlayServices" );
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

       // this.startService(new Intent(this, LocationService.class));
        if (mSlidingTabLayout != null) {
            mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset,
                                           int positionOffsetPixels) {
                }
                @Override
                public void onPageSelected(int position) {
                    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mSlidingTabLayout.announceForAccessibility(
                                getString(R.string.my_schedule_page_desc_a11y,
                                        getDayName(position)));
                    }*/
                }
                @Override
                public void onPageScrollStateChanged(int state) {
                    //enableDisableSwipeRefresh(state == ViewPager.SCROLL_STATE_IDLE);
                }
            });
        }

        registerHideableHeaderView(findViewById(R.id.toolbar_actionbar));
        overridePendingTransition(0, 0);



    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
    }

    public Location getLocation(){
        if(isBound) return mLocationService.getLocation();
        return null;
    }

    public final Object getSyncToken(){
        return syncToken;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");

        this.registerReceiver(NetworkController.get(this), NetworkController.make());
        checkPlayServices();

    }

    @Override
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(NetworkController.get(this));

    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (syncToken){
                LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
                mLocationService = binder.getService();
                isBound = true;
                syncToken.notifyAll();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MAIN;
    }

    private class OurViewPagerAdapter extends FragmentPagerAdapter {
            public OurViewPagerAdapter(FragmentManager fm) {
                super(fm);
            }
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return new OverviewFragment();
                case 1:
                    return new CheckinFragment();
                case 2:
                    GroupListFragment fragment = new GroupListFragment();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("allGroups", false);
                    fragment.setArguments(bundle);
                    return fragment;
            }
            OverviewFragment frag = new OverviewFragment();
            return frag;
        }
        @Override
        public int getCount() {
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.main_tabs)[position];
        }
    }

    /************************************************************
                                GCM
     ************************************************************/
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGcmPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGcmPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

            }
        }.execute(null, null, null);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send
     * messages to your app. Not needed for this demo since the device sends upstream messages
     * to a server that echoes back the message using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        Log.d(TAG,"sendRegistrationIdToBackend");
        new Thread(){
            @Override
            public void run() {
                try {
                    SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.sharedprefs), Context.MODE_PRIVATE);

                    while((prefs.getString(context.getString(R.string.backendtoken), "No token")).
                            equals("No token")){
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            Log.d(TAG, "Thread woken from sleep in sendRegistrationIdToBackend");
                            e.printStackTrace();
                            return;
                        }
                    }
                    String token = prefs.getString(context.getString(R.string.backendtoken), "No token");
                    Log.d(TAG,"sendRegistrationIdToBackend run: backendToken:"+token+ " regId:"+regid);
                    BackendAPI.get(getApplicationContext()).registerGcmId(regid);
                } catch (IOException e) {}
            }
        }.start();
    }

}