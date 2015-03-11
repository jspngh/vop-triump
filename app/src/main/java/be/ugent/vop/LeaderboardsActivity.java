package be.ugent.vop;

import android.os.Bundle;

import com.facebook.Session;
import com.facebook.SessionState;

/**
 * Created by siebe on 25/02/15.
 */
public class LeaderboardsActivity extends BaseActivity {

    @Override
    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        overridePendingTransition(0, 0);
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_LEADERBOARDS;
    }
}
