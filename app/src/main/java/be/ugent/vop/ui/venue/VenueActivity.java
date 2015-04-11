package be.ugent.vop.ui.venue;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.nineoldandroids.view.ViewHelper;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.VenueInfoLoader;
import be.ugent.vop.foursquare.FoursquareVenue;
import be.ugent.vop.ui.event.NewEventActivity;
import be.ugent.vop.ui.main.OverviewFragment;
import be.ugent.vop.ui.widget.SlidingTabLayout;
import be.ugent.vop.utils.PrefUtils;

/**
 * Created by vincent on 03/03/15.
 */
public class VenueActivity extends BaseActivity implements VenueRankingFragment.VenueRankingCallback {
    private static final String TAG = "VenueActivity";

    public static final String VENUE_ID = "venueID";

    private ImageView venueImageView;
    private Context context;
    private String fsVenueId;
    private Palette mPalette;
    private View mHeader;

    // View pager and adapter (for narrow mode)
    ViewPager mViewPager = null;
    OurViewPagerAdapter mViewPagerAdapter = null;
    SlidingTabLayout mSlidingTabLayout = null;
    private int mActionBarHeight;
    private int mMinHeaderHeight;
    private int mHeaderHeight;
    private int mMinHeaderTranslation;

    private TypedValue mTypedValue = new TypedValue();

    @Override
    public Palette getPalette() {
        return mPalette;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (mViewPager.getCurrentItem() == 0) {
            int scrollY = getScrollY(absListView);
            ViewHelper.setTranslationY(mHeader, Math.max(-scrollY, mMinHeaderTranslation));
            //float ratio = clamp(ViewHelper.getTranslationY(mHeader) / mMinHeaderTranslation, 0.0f, 1.0f);
            //interpolate(mHeaderLogo, getActionBarIconView(), sSmoothInterpolator.getInterpolation(ratio));
            //setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int getActionBarHeight() {
        if (mActionBarHeight != 0) {
            return mActionBarHeight;
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
            getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
        }else{
            getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);
        }
        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, getResources().getDisplayMetrics());
        return mActionBarHeight;
    }

    public int getScrollY(AbsListView view) {
        View c = view.getChildAt(0);
        if (c == null) {
            return 0;
        }
        int firstVisiblePosition = view.getFirstVisiblePosition();
        int top = c.getTop();
        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = mHeaderHeight;
        }
        return -top + firstVisiblePosition * c.getHeight() + headerHeight;
    }

    public void onCreate(Bundle savedInstanceState){
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue);

        mMinHeaderHeight = getResources().getDimensionPixelSize(R.dimen.min_header_height);
        mHeaderHeight = getResources().getDimensionPixelSize(R.dimen.header_height);
        mMinHeaderTranslation = -mMinHeaderHeight + getActionBarHeight();

        context = getApplicationContext();

        mHeader = findViewById(R.id.header);
        venueImageView = (ImageView) findViewById(R.id.imageView);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);

        if(getIntent().getExtras().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getIntent().getExtras().getString(VenueActivity.VENUE_ID);

        mViewPagerAdapter = new OurViewPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mViewPagerAdapter);

        mSlidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        //setSlidingTabLayoutContentDescriptions();
        Resources res = getResources();
        mSlidingTabLayout.setSelectedIndicatorColors(res.getColor(R.color.venue_tab_selected_strip));
        mSlidingTabLayout.setDistributeEvenly(true);
        mSlidingTabLayout.setViewPager(mViewPager);

        /**
         *
         * Initialize loaders
         */
        //loader for venueInfo (to foursquare)
        getLoaderManager().initLoader(2, null, mVenueInfoLoaderListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.venue_menu, menu);

       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_new_event:
                Intent intent = new Intent(this,NewEventActivity.class);
                intent.putExtra(VenueActivity.VENUE_ID, fsVenueId);

                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_OTHER;
    }

    private class OurViewPagerAdapter extends FragmentPagerAdapter {
        public OurViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    VenueRankingFragment fragment = new VenueRankingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(VenueActivity.VENUE_ID, fsVenueId);
                    fragment.setArguments(bundle);
                    return fragment;
                case 1:
                    VenueEventFragment fragment2 = new VenueEventFragment();
                    Bundle bundle2 = new Bundle();
                    bundle2.putString(VenueActivity.VENUE_ID, fsVenueId);
                    fragment2.setArguments(bundle2);
                    return fragment2;
            }
            OverviewFragment frag = new OverviewFragment();
            return frag;
        }
        @Override
        public int getCount() {
            return 2;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.venue_tabs)[position];
        }
    }

    /***********
     Loaders
     ***********/
    /**
     *
     * Loader 3: VenueInfo
     *
     */

    private LoaderManager.LoaderCallbacks<FoursquareVenue> mVenueInfoLoaderListener
            = new LoaderManager.LoaderCallbacks<FoursquareVenue>() {
        @Override
        public void onLoadFinished(Loader<FoursquareVenue> loader, FoursquareVenue venue) {
            Log.d("VenueFragment", "onLoadFinished of venueInfoLoader");
            if(venue!=null) {
                setTitle(venue.getName());
                //titleTextView.setText(venue.getName());
                //placeholder image
                String photoUrl;
                if(venue.getPhotos().size()>0)
                    photoUrl = venue.getPhotos().get(0).getPrefix() + "500x500" + venue.getPhotos().get(0).getSuffix();
                else photoUrl =
                        "http://iahip.org/wp-content/plugins/jigoshop/assets/images/placeholder.png";

                Ion.with(context)
                        .load(photoUrl)
                        .withBitmap()
                        .asBitmap()
                        .setCallback(new FutureCallback<Bitmap>() {
                            @Override
                            public void onCompleted(Exception e, Bitmap result) {
                                Palette p = Palette.generate(result);
                                mPalette = p;

                                venueImageView.setImageBitmap(result);

                                Log.d(TAG, "Palette for image created, setting");
                                venueImageView.setColorFilter(p.getMutedColor(Color.rgb(100, 100, 100)), PorterDuff.Mode.MULTIPLY);
                            }
                        });
            }
        }

        @Override
        public Loader<FoursquareVenue> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            VenueInfoLoader loader = new VenueInfoLoader(context, fsVenueId);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<FoursquareVenue> loader) {}

    };
}
