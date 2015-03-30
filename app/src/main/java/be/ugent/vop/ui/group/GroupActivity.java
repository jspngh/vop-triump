package be.ugent.vop.ui.group;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;

public class GroupActivity extends BaseActivity implements GroupFragment.OnFragmentInteractionListener {

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
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, groupFragment);
                fragmentTransaction.commit();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    super.onBackPressed();
                } else {
                    getFragmentManager().popBackStack();
                }
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onGroupFragmentInteraction(long groupId){
        AcceptMembersFragment acceptMembersFragment = new AcceptMembersFragment();
        Bundle args = new Bundle();
        args.putLong(GroupFragment.GROUP_ID, groupId);
        acceptMembersFragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, acceptMembersFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }
}
