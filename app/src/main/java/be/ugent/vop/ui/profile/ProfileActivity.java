package be.ugent.vop.ui.profile;

import android.os.Bundle;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.utils.PrefUtils;

public class ProfileActivity extends BaseActivity implements ProfileFragment.OnFragmentInteractionListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (findViewById(R.id.fragment_container) != null) {

            if (savedInstanceState != null) {
                return;
            }

            Bundle mBundle = getIntent().getExtras();
            ProfileFragment mProfileFragment = new ProfileFragment();
            mProfileFragment.setArguments(mBundle);
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mProfileFragment).commit();
        }
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_OTHER;
    }

    public void onProfileFragmentInteraction(){

    }
}
