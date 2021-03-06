/*
 * Copyright 2014 Google Inc. All rights reserved.
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
 * limitations under the License.
 */

package be.ugent.vop;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Intent;

import android.content.Loader;
import android.graphics.Color;
import android.location.Location;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;


import be.ugent.vop.backend.loaders.OverviewLoader;
import be.ugent.vop.backend.loaders.VenueLoader;
import be.ugent.vop.feedback.Feedback;
import be.ugent.vop.feedback.FeedbackActivity;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.main.OverviewAdapter;
import be.ugent.vop.ui.reward.RewardsActivity;
import be.ugent.vop.ui.event.EventActivity;
import be.ugent.vop.ui.group.GroupListActivity;
import be.ugent.vop.ui.leaderboard.LeaderboardsActivity;
import be.ugent.vop.ui.login.LoginActivity;
import be.ugent.vop.ui.login.LoginFragment;
import be.ugent.vop.ui.main.MainActivity;
import be.ugent.vop.ui.profile.ProfileActivity;
import be.ugent.vop.ui.profile.ProfileFragment;

import be.ugent.vop.ui.settings.SettingsActivity;

import be.ugent.vop.utils.LUtils;
import be.ugent.vop.utils.PrefUtils;

import static be.ugent.vop.utils.LogUtils.makeLogTag;

/**
 * A base activity that handles common functionality in the app. This includes the
 * navigation drawer, login and authentication, Action Bar tweaks, amongst others.
 */
public abstract class BaseActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, LoaderManager.LoaderCallbacks<ArrayList<FoursquareVenue>> {
    private static final String TAG = makeLogTag(BaseActivity.class);

    // Navigation drawer:
    private DrawerLayout mDrawerLayout;

    private ObjectAnimator mStatusBarColorAnimator;
    private ViewGroup mDrawerItemsListContainer;


    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();

    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_MAIN = 0;
    protected static final int NAVDRAWER_ITEM_LEADERBOARDS = 1;
    protected static final int NAVDRAWER_ITEM_REWARD = 2;
    protected static final int NAVDRAWER_ITEM_EVENT = 3;
    protected static final int NAVDRAWER_ITEM_GROUPS = 4;
    protected static final int NAVDRAWER_ITEM_FEEDBACK = 5;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 6;
    protected static final int NAVDRAWER_ITEM_LOGOUT = 7;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;
    protected static final int NAVDRAWER_ITEM_OTHER = -4;

    protected static final int LOCATION_NOT_UPDATING = 0;
    protected static final int LOCATION_UPDATING_FAST = 1;
    protected static final int LOCATION_UPDATING_SLOW = 2;

    public static final int MIN_LOCATION_ACCURACY = 200;
    protected static final int HOUR_MILLIS = 3600000;


    // titles for navdrawers items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.navdrawer_item_main,
            R.string.navdrawer_item_leaderboards,
            R.string.navdrawer_item_reward,
            R.string.navdrawer_item_event,
            R.string.navdrawer_item_groups,
            R.string.navdrawer_item_feedback,
            R.string.navdrawer_item_settings,
            R.string.navdrawer_item_logout
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.ic_drawer_people_met,  // My Schedule
            R.drawable.ic_drawer_leaderboard,
            R.drawable.ic_drawer_people_met,
            R.drawable.ic_drawer_event,
            R.drawable.ic_drawer_people_met,
            R.drawable.ic_drawer_feedback,
            R.drawable.ic_drawer_settings,
            R.drawable.ic_drawer_logout
    };

    // delay to launch nav drawer item, to allow close animation to play
    private static final int NAVDRAWER_LAUNCH_DELAY = 250;

    // fade in and fade out durations for the main content when switching between
    // different Activities of the app through the Nav Drawer
    private static final int MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final int MAIN_CONTENT_FADEIN_DURATION = 250;

    // list of navdrawer items that were actually added to the navdrawer, in order
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<>();

    // views that correspond to each navdrawer item, null if not yet created
    private View[] mNavDrawerItemViews = null;


    // Primary toolbar and drawer toggle
    private Toolbar mActionBarToolbar;

    // asynctask that performs GCM registration in the backgorund
    private AsyncTask<Void, Void, Void> mGCMRegisterTask;

    // handle to our sync observer (that notifies us about changes in our sync state)
    private Object mSyncObserverHandle;


    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private boolean mActionBarAutoHideEnabled = false;
    private int mActionBarAutoHideSensivity = 0;
    private int mActionBarAutoHideMinY = 0;
    private int mActionBarAutoHideSignal = 0;
    private boolean mActionBarShown = true;

    // Helper methods for L APIs
    private LUtils mLUtils;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;

    private int mThemedStatusBarColor;
    private int mNormalStatusBarColor;
    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();
    private ActionBarDrawerToggle mDrawerToggle;
    private Handler mHandler;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private int mRequestingLocationUpdates = LOCATION_NOT_UPDATING;


    private ArrayList<LocationUpdateListener> locationUpdateListeners = new ArrayList<>();
    private LocationRequest mLocationRequest;
    private Date mLastUpdated;

    private ArrayList<VenueListListener> venueListListeners = new ArrayList<>();
    private ArrayList<FoursquareVenue> venues;

    public interface LocationUpdateListener{
        public void locationUpdated(Location newLocation, Date lastUpdated);
    }

    public interface VenueListListener{
        public void newVenuesAvailable(ArrayList<FoursquareVenue> venues);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //buildGoogleApiClient();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mHandler = new Handler();

        mLUtils = LUtils.getInstance(this);
        mThemedStatusBarColor = getResources().getColor(R.color.theme_primary_dark);
        mNormalStatusBarColor = mThemedStatusBarColor;

        startLoginProcess();
    }


    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Sets up the navigation drawer as appropriate. Note that the nav drawer will be
     * different depending on whether the attendee indicated that they are attending the
     * event on-site vs. attending remotely.
     */
    private void setupNavDrawer() {
        // What nav drawer item should be selected?
        int selfItem = getSelfNavDrawerItem();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        mDrawerLayout.setStatusBarBackgroundColor(
                getResources().getColor(R.color.theme_primary_dark));
        LinearLayout navDrawer = (LinearLayout)
                mDrawerLayout.findViewById(R.id.navdrawer);
        if (selfItem == NAVDRAWER_ITEM_INVALID) {
            // do not show a nav drawer
            if (navDrawer != null) {
                ((ViewGroup) navDrawer.getParent()).removeView(navDrawer);
            }
            mDrawerLayout = null;
            return;
        }

        if (mActionBarToolbar != null && getSelfNavDrawerItem() != NAVDRAWER_ITEM_OTHER) {
            // ActionBarDrawerToggle ties together the the proper interactions
            // between the sliding drawer and the action bar app icon
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,                  /* host Activity */
                    mDrawerLayout,         /* DrawerLayout object */
                    R.string.drawer_open,  /* "open drawer" description for accessibility */
                    R.string.drawer_close  /* "close drawer" description for accessibility */
            ) {
                public void onDrawerClosed(View view) {
                    // getSupportActionBar().setTitle(mTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }

                public void onDrawerOpened(View drawerView) {
                    //getSupportActionBar().setTitle(mDrawerTitle);
                    invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                }
            };


            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }else{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                onNavDrawerStateChanged(false, false);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                onNavDrawerStateChanged(true, false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                onNavDrawerStateChanged(isNavDrawerOpen(), newState != DrawerLayout.STATE_IDLE);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                onNavDrawerSlide(slideOffset);
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        setupAccountBox();
        // populate the nav drawer with the correct items
        populateNavDrawer();
    }

    private void setupAccountBox() {

        final View chosenAccountView = findViewById(R.id.chosen_account_view);

        ImageView coverImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_cover_image);
        ImageView profileImageView = (ImageView) chosenAccountView.findViewById(R.id.profile_image);
        TextView nameTextView = (TextView) chosenAccountView.findViewById(R.id.profile_name_text);
        TextView emailTextView = (TextView) chosenAccountView.findViewById(R.id.profile_email_text);

        String displayName = PrefUtils.getUserDisplayName(this);
        //String lastName = PrefUtils.getUserLastName(this);
        if (displayName == null) {
            nameTextView.setVisibility(View.GONE);
        } else {
            nameTextView.setVisibility(View.VISIBLE);
            StringBuilder builder = new StringBuilder(displayName);
//            if(lastName != null)
//                builder.append(" ").append(lastName);
            nameTextView.setText(builder.toString());
        }

        String profilePicPrefix = PrefUtils.getProfilePicPrefix(this);
        String profilePicSuffix = PrefUtils.getProfilePicSuffix(this);
        if (profilePicPrefix != null) {
            String profilePic = profilePicPrefix + "300x300" + profilePicSuffix;
            Picasso.with(getApplicationContext())
                    .load(profilePic)
                    .fit().centerCrop()
                    .placeholder(R.drawable.person_image_empty)
                    .into(profileImageView);
        }


        Picasso.with(getApplicationContext())
                .load(R.drawable.dummy_venue)
                .fit().centerCrop()
                .placeholder(R.drawable.person_image_empty)
                .into(coverImageView);

        String email = PrefUtils.getUserEmail(this);
        if(email == null){
            emailTextView.setVisibility(View.GONE);
        }else{
            emailTextView.setVisibility(View.VISIBLE);
            emailTextView.setText(email);
        }

        chosenAccountView.setEnabled(true);

        String userId = PrefUtils.getUserId(this);
        final Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(ProfileFragment.USER_ID, userId);

        chosenAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(intent);
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        getActionBarToolbar();
    }

    // Subclasses can override this for custom behavior
    protected void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating) {
        if (mActionBarAutoHideEnabled && isOpen) {
            autoShowOrHideActionBar(true);
        }
    }

    protected void onNavDrawerSlide(float offset) {}

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START);
    }

    protected void closeNavDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(Gravity.START);
        }
    }

    /** Populates the navigation drawer with the appropriate items. */
    private void populateNavDrawer() {
        mNavDrawerItems.clear();

        mNavDrawerItems.add(NAVDRAWER_ITEM_MAIN);
        mNavDrawerItems.add(NAVDRAWER_ITEM_LEADERBOARDS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_REWARD);
        mNavDrawerItems.add(NAVDRAWER_ITEM_EVENT);
        mNavDrawerItems.add(NAVDRAWER_ITEM_GROUPS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR_SPECIAL);
        mNavDrawerItems.add(NAVDRAWER_ITEM_FEEDBACK);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_LOGOUT);

        createNavDrawerItems();
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
        }
        super.onBackPressed();
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();

        View mainContent = findViewById(R.id.main_content);
        if (mainContent != null) {
            mainContent.setAlpha(0);
            mainContent.animate().alpha(1).setDuration(MAIN_CONTENT_FADEIN_DURATION);
        } else {
            Log.w(TAG, "No view with ID main_content to fade in.");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch(item.getItemId()){
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToNavDrawerItem(int item) {
        Intent intent;
        switch (item) {
            case NAVDRAWER_ITEM_MAIN:
                intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_LEADERBOARDS:
                intent = new Intent(this, LeaderboardsActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_REWARD:
                intent = new Intent(this, RewardsActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_EVENT:
                intent = new Intent(this, EventActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_GROUPS:
                intent = new Intent(this, GroupListActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_LOGOUT:
                intent = new Intent(this, LoginActivity.class);
                intent.putExtra(LoginFragment.LOGIN_ACTION, LoginFragment.LOGOUT);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_FEEDBACK:
                intent = new Intent(this, FeedbackActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void onNavDrawerItemClicked(final int itemId) {
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }

        if (isSpecialItem(itemId)) {
            goToNavDrawerItem(itemId);
        } else {
            // launch the target Activity after a short delay, to allow the close animation to play
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(itemId);
                }
            }, NAVDRAWER_LAUNCH_DELAY);

            // change the active item on the list so the user can see the item changed
            setSelectedNavDrawerItem(itemId);
            // fade out the main content
            View mainContent = findViewById(R.id.main_content);
            if (mainContent != null) {
                mainContent.animate().alpha(0).setDuration(MAIN_CONTENT_FADEOUT_DURATION);
            }
        }

        mDrawerLayout.closeDrawer(Gravity.START);
    }


    /**
     * Location services
     */

    protected synchronized void buildGoogleApiClient() {
        Log.d(TAG, "Build API client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Google API connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null){
            notifyLocationUpdateListeners();
            getLoaderManager().initLoader(0, null, this);
            mLastUpdated = new Date();
        }

        // Continuous location updates
        startLocationUpdates(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection to Google play services failed");
    }

    @Override
    public void onLocationChanged(Location location){
        Log.d(TAG, "Location changed");

        if(mLastLocation == null
                || (location.getAccuracy() < BaseActivity.MIN_LOCATION_ACCURACY
                && mLastLocation.distanceTo(location) > 100)){
            mLastLocation = location;
            mLastUpdated = new Date();

            notifyLocationUpdateListeners();
            getLoaderManager().initLoader(0, null, this);
        }

        if(mLastLocation.getAccuracy() < MIN_LOCATION_ACCURACY && mRequestingLocationUpdates != LOCATION_UPDATING_SLOW)
            startLocationUpdates(false);
    }

    protected void createFastLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void createSlowLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(60000);
        mLocationRequest.setFastestInterval(60000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected synchronized void startLocationUpdates(boolean fast) {
        Log.d(TAG, "Start location updates, " + (fast?"Fast" : "slow"));
        if(mRequestingLocationUpdates != LOCATION_NOT_UPDATING)
            stopLocationUpdates();

        if(fast){
            mRequestingLocationUpdates = LOCATION_UPDATING_FAST;
            createFastLocationRequest();
        }else{
            mRequestingLocationUpdates = LOCATION_UPDATING_SLOW;
            createSlowLocationRequest();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected synchronized void stopLocationUpdates() {
        Log.d(TAG, "Stop location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = LOCATION_NOT_UPDATING;
    }

    private void notifyLocationUpdateListeners(){
        for(LocationUpdateListener l : locationUpdateListeners)
            l.locationUpdated(mLastLocation, mLastUpdated);
    }

    public void addLocationUpdateListener(LocationUpdateListener listener){
        locationUpdateListeners.add(listener);
        if(mLastLocation != null)
            listener.locationUpdated(mLastLocation, mLastUpdated);
    }

    public void removeLocationUpdateListener(LocationUpdateListener listener){
        locationUpdateListeners.remove(listener);
    }

    public void addVenueListListener(VenueListListener listener){
        venueListListeners.add(listener);
        if(venues != null)
            listener.newVenuesAvailable(venues);
    }

    public void removeVenueListListener(VenueListListener listener){
        venueListListeners.remove(listener);
    }

    @Override
    public Loader<ArrayList<FoursquareVenue>> onCreateLoader(int i, Bundle bundle) {
        return new VenueLoader(this, mLastLocation);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<FoursquareVenue>> loader, final ArrayList<FoursquareVenue> venues) {
        for(VenueListListener listener : venueListListeners)
            listener.newVenuesAvailable(venues);
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<FoursquareVenue>> loader) {
        venues = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
        else {

            if (mLastUpdated != null && mLastUpdated.getTime() < new Date().getTime() - HOUR_MILLIS)
                startLocationUpdates(true); // Too long ago, get new accurate location
            else if (mLastLocation != null && mLastLocation.getAccuracy() < MIN_LOCATION_ACCURACY)
                startLocationUpdates(false); // Not too long ago and good accuracy
            else
                startLocationUpdates(true); // No good accuracy
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected())
            stopLocationUpdates();

        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
    }


    private void startLoginProcess() {
        String FStoken = PrefUtils.getBackendToken(this);
        String backendToken = PrefUtils.getBackendToken(this);

        if(FStoken == null){
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra(LoginFragment.LOGIN_ACTION, LoginFragment.LOGIN_FS);
            startActivity(loginIntent);
            finish();
        }else if(backendToken == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.putExtra(LoginFragment.LOGIN_ACTION, LoginFragment.LOGIN_BACKEND);
            startActivity(loginIntent);
            finish();
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /**
     * Initializes the Action Bar auto-hide (aka Quick Recall) effect.
     */
    private void initActionBarAutoHide() {
        mActionBarAutoHideEnabled = true;
        mActionBarAutoHideMinY = getResources().getDimensionPixelSize(
                R.dimen.action_bar_auto_hide_min_y);
        mActionBarAutoHideSensivity = getResources().getDimensionPixelSize(
                R.dimen.action_bar_auto_hide_sensivity);
    }

    /**
     * Indicates that the main content has scrolled (for the purposes of showing/hiding
     * the action bar for the "action bar auto hide" effect). currentY and deltaY may be exact
     * (if the underlying view supports it) or may be approximate indications:
     * deltaY may be INT_MAX to mean "scrolled forward indeterminately" and INT_MIN to mean
     * "scrolled backward indeterminately".  currentY may be 0 to mean "somewhere close to the
     * start of the list" and INT_MAX to mean "we don't know, but not at the start of the list"
     */
    private void onMainContentScrolled(int currentY, int deltaY) {
        if (deltaY > mActionBarAutoHideSensivity) {
            deltaY = mActionBarAutoHideSensivity;
        } else if (deltaY < -mActionBarAutoHideSensivity) {
            deltaY = -mActionBarAutoHideSensivity;
        }

        if (Math.signum(deltaY) * Math.signum(mActionBarAutoHideSignal) < 0) {
            // deltaY is a motion opposite to the accumulated signal, so reset signal
            mActionBarAutoHideSignal = deltaY;
        } else {
            // add to accumulated signal
            mActionBarAutoHideSignal += deltaY;
        }

        boolean shouldShow = currentY < mActionBarAutoHideMinY ||
                (mActionBarAutoHideSignal <= -mActionBarAutoHideSensivity);

        Log.d("ACTIONBAR", shouldShow?"yes":"no");
        autoShowOrHideActionBar(shouldShow);
    }

    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown) {
            return;
        }

        mActionBarShown = show;
        onActionBarAutoShowOrHide(show);
    }

    protected void enableActionBarAutoHide(final ListView listView) {
        initActionBarAutoHide();
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            final static int ITEMS_THRESHOLD = 3;
            int lastFvi = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                onMainContentScrolled(firstVisibleItem <= ITEMS_THRESHOLD ? 0 : Integer.MAX_VALUE,
                        lastFvi - firstVisibleItem > 0 ? Integer.MIN_VALUE :
                                lastFvi == firstVisibleItem ? 0 : Integer.MAX_VALUE
                );
                lastFvi = firstVisibleItem;
            }
        });
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == itemId;
        int layoutToInflate = 0;
        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else if (itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            // we are done
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ?
                NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ?
                NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        return view;
    }

    private boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_SETTINGS;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        if (selected) {
            view.setBackgroundResource(R.drawable.selected_navdrawer_item_background);
        }

        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView);
        }
    }
    protected void deregisterHideableHeaderView(View hideableHeaderView) {
        if (mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.remove(hideableHeaderView);
        }
    }

    public LUtils getLUtils() {
        return mLUtils;
    }

    public int getThemedStatusBarColor() {
        return mThemedStatusBarColor;
    }
    public void setNormalStatusBarColor(int color) {
        mNormalStatusBarColor = color;
        if (mDrawerLayout != null) {
            mDrawerLayout.setStatusBarBackgroundColor(mNormalStatusBarColor);
        }
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }
        mStatusBarColorAnimator = ObjectAnimator.ofInt(
                (mDrawerLayout != null) ? mDrawerLayout : mLUtils,
                (mDrawerLayout != null) ? "statusBarBackgroundColor" : "statusBarColor",
                shown ? Color.BLACK : mNormalStatusBarColor,
                shown ? mNormalStatusBarColor : Color.BLACK)
                .setDuration(250);
        if (mDrawerLayout != null) {
            mStatusBarColorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    ViewCompat.postInvalidateOnAnimation(mDrawerLayout);
                }
            });
        }
        mStatusBarColorAnimator.setEvaluator(ARGB_EVALUATOR);
        mStatusBarColorAnimator.start();

        for (View view : mHideableHeaderViews) {
            if (shown) {
                view.animate()
                        .translationY(0)
                        .alpha(1)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.animate()
                        .translationY(-view.getBottom())
                        .alpha(0)
                        .setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            }
        }
    }

    public void setTitle(String title){
        getSupportActionBar().setTitle(title);
    }
}
