package be.ugent.vop.ui.leaderboard;

import android.os.Bundle;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;

/**
 * Created by siebe on 25/02/15.
 */
public class LeaderboardsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        LeaderboardsFragment fragment = new LeaderboardsFragment();
        Bundle venueBundle = getIntent().getExtras();

        fragment.setArguments(venueBundle);

        this.getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_LEADERBOARDS;
    }
}
