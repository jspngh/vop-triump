package be.ugent.vop.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.ui.leaderboard.LeaderboardsFragment;

public class GroupListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboards);

        if (findViewById(R.id.fragment_container) != null) {
            GroupListFragment fragment = new GroupListFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("allGroups", true);
            fragment.setArguments(bundle);

            this.getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_GROUPS;
    }
}
