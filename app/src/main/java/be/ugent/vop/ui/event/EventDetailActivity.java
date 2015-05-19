package be.ugent.vop.ui.event;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.EventRankingLoader;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.ui.venue.RankingAdapter;
import be.ugent.vop.utils.PrefUtils;


public class EventDetailActivity extends ActionBarActivity {

    public static final String EVENT_NAME = "name";
    public static final String EVENT_REWARD = "reward";
    public static final String EVENT_LOCATION = "location";
    public static final String EVENT_GROUPS = "groups";
    public static final String EVENT_FROM = "from";
    public static final String EVENT_TO = "to";
    public static final String EVENT_ID = "eventid";

    private TextView groups;
    private TextView reward;
    private TextView info;
    private TextView time;
    private RecyclerView rankingView;
    private RankingAdapter mAdapter;

    private long eventId;
    private Context context;

    public LoaderManager.LoaderCallbacks<List<RankingBean>> mRankingLoaderListener
            = new LoaderManager.LoaderCallbacks<List<RankingBean>>() {

        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> rankings) {
            Log.d("EventDetailActivity", "onLoadFinished rankingloader");

            if(rankings!=null){
                rankingView.setVisibility(View.VISIBLE);

                for(RankingBean r:rankings){
                    Log.d("VenueFragment",r.getGroupBean().getName()+ " | "+r.getPoints());
                }

                int ht_px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, getResources().getDisplayMetrics());
                rankingView.getLayoutParams().height = ht_px * rankings.size();

                mAdapter.setRankings(rankings);
                mAdapter.notifyDataSetChanged();
            }else{
                rankingView.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
            Log.d("EventDetailActivity", "onCreateLoader");
            return new EventRankingLoader(context, eventId);
        }

        @Override
        public void onLoaderReset(Loader<List<RankingBean>> loader) {
            //rankingListView.setAdapter(null);
        }
    };
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        getActionBarToolbar();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        overridePendingTransition(0, 0);

        context = this;

        groups = (TextView) findViewById(R.id.event_groups);
        reward = (TextView) findViewById(R.id.event_reward);
        info = (TextView) findViewById(R.id.event_info);
        time = (TextView) findViewById(R.id.event_time);
        rankingView = (RecyclerView) findViewById(R.id.ranking_list);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        rankingView.setLayoutManager(mLayoutManager);
        rankingView.setItemAnimator(new DefaultItemAnimator());

        int ht = 200;
        int wt = 200;

        float ht_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ht, getResources().getDisplayMetrics());
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, wt, getResources().getDisplayMetrics());

        mAdapter = new RankingAdapter(this, null, Color.BLACK, Color.WHITE, (int)wt_px, (int)ht_px, false);
        rankingView.setAdapter(mAdapter);


        Bundle extras = getIntent().getExtras();

        if(extras != null){
            reward.setText(extras.getString(EVENT_REWARD));
            info.setText(extras.getString(EVENT_LOCATION));
            groups.setText(extras.getString(EVENT_GROUPS));
            time.setText("From: " + extras.getString(EVENT_FROM) + "\nTo: " + extras.getString(EVENT_TO));

            setTitle(extras.getString(EVENT_NAME));

            eventId = extras.getLong(EVENT_ID);

            getLoaderManager().restartLoader(100, null, mRankingLoaderListener);
        }
    }

    private Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
