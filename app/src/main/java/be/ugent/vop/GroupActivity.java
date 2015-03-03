package be.ugent.vop;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class GroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        registerHideableHeaderView(findViewById(R.id.toolbar_actionbar));
        overridePendingTransition(0, 0);

        if (findViewById(R.id.fragment_container) != null) {
//            start this activity as follows:
//            Intent intent = new Intent(getActivity(), GroupActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putInt("groupId", 0);
//            intent.putExtras(bundle);
//            startActivity(intent);
//            getActivity().finish();
            GroupFragment groupListFragment = new GroupFragment();
            groupListFragment.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, groupListFragment).commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_OTHER;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // don't create an optionsmenu yet
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}