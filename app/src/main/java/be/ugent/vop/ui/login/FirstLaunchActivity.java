package be.ugent.vop.ui.login;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import be.ugent.vop.R;
import be.ugent.vop.utils.ParallaxPageTransformer;
import me.relex.circleindicator.CircleIndicator;

public class FirstLaunchActivity extends ActionBarActivity {
    private static final String TAG = "FirstLaunchActivity";
    private static final int NUM_PAGES = 3;
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_launch);

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mPagerAdapter = new TutorialAdapter(getFragmentManager());
        CircleIndicator Indicator = (CircleIndicator) findViewById(R.id.circle_indicator);
        mViewPager.setAdapter(mPagerAdapter);

        ParallaxPageTransformer pageTransformer = new ParallaxPageTransformer()
                .addViewToParallax(new ParallaxPageTransformer.ParallaxTransformInformation(R.id.background, 2, 2))
                .addViewToParallax(new ParallaxPageTransformer.ParallaxTransformInformation(R.id.image, -0.65f,
                        ParallaxPageTransformer.ParallaxTransformInformation.PARALLAX_EFFECT_DEFAULT))
                .addViewToParallax(new ParallaxPageTransformer.ParallaxTransformInformation(R.id.bannerImage, 2, 2))
                .addViewToParallax(new ParallaxPageTransformer.ParallaxTransformInformation(R.id.back, -0.65f,
                        ParallaxPageTransformer.ParallaxTransformInformation.PARALLAX_EFFECT_DEFAULT));
        mViewPager.setPageTransformer(true, pageTransformer);
        Indicator.setViewPager(mViewPager);
    }

    private class TutorialAdapter extends FragmentPagerAdapter {

        public TutorialAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public Fragment getItem(int position) {
            return TutorialFragment.newInstance(position);
        }
    }
}
