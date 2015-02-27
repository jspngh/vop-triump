package be.ugent.vop.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;


import be.ugent.vop.BaseActivity;
import be.ugent.vop.GroupFragment;
import be.ugent.vop.R;

import be.ugent.vop.VenueFragment;
import be.ugent.vop.ui.widget.SlidingTabLayout;

public class MainActivity extends BaseActivity {

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
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_MAIN;
    }

    private class OurViewPagerAdapter extends FragmentPagerAdapter {
        public OurViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            //LOGD(TAG, "Creating fragment #" + position);
            switch(position){
                case 0:
                    return new OverviewFragment();
                case 2:
                    return new VenueFragment();
            }
            GroupFragment frag = new GroupFragment();
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