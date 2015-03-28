package be.ugent.vop.ui.group;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;

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
//            bundle.putLong("groupId", 0);
//            intent.putExtras(bundle);
//            startActivity(intent);
//            getActivity().finish();
            Bundle b = getIntent().getExtras();
            if(b!=null) {
                GroupFragment groupFragment = new GroupFragment();
                groupFragment.setArguments(getIntent().getExtras());
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, groupFragment).commit();
            }else{
                GroupNewFragment groupNewFragment = new GroupNewFragment();
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, groupNewFragment).commit();
            }
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
