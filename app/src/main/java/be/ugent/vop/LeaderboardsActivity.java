package be.ugent.vop;

import android.os.Bundle;

/**
 * Created by siebe on 25/02/15.
 */
public class LeaderboardsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_LEADERBOARDS;
    }
}
