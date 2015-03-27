package be.ugent.vop.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.LocationService;
import be.ugent.vop.NetworkController;
import be.ugent.vop.R;
import be.ugent.vop.ui.group.GroupListFragment;
import be.ugent.vop.ui.venue.CheckinFragment;
import be.ugent.vop.ui.widget.SlidingTabLayout;

public class MainActivity extends BaseActivity {
    private LocationService mLocationService;
    private boolean isBound = false;
    public final Object syncToken = new Object();

    // View pager and adapter (for narrow mode)
    ViewPager mViewPager = null;
    OurViewPagerAdapter mViewPagerAdapter = null;
    SlidingTabLayout mSlidingTabLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mViewPagerAdapter = new OurViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        //setSlidingTabLayoutContentDescriptions();
        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        this.startService(new Intent(this, LocationService.class));
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

    public LocationService getLocationService(){
        return mLocationService;
    }

    public final Object getSyncToken(){
        return syncToken;
    }

    @Override
    public void onResume() {
        super.onResume();
        this.registerReceiver(NetworkController.get(this), NetworkController.make());

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
                    return new GroupListFragment();
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

}