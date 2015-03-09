package be.ugent.vop.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import be.ugent.vop.R;
import be.ugent.vop.ui.main.MainActivity;

public class LoginActivity extends Activity implements LoginFragment.OnFragmentInteractionListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            Bundle mBundle = getIntent().getExtras();
            if (!mBundle.getString(getString(R.string.profile), "").equals(ProfileFragment.PROFILE_ACTIVITY)){
                // Create a new Fragment to be placed in the activity layout
                LoginFragment loginFragment = new LoginFragment();

                // Add the fragment to the 'fragment_container' FrameLayout
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, loginFragment).commit();
            }
            else if(mBundle.getLong(ProfileFragment.USER_ID, 0) != 0){
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(mBundle);
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, profileFragment).commit();

            }
        }
    }

    public void onLoginFragmentInteraction(){
        SharedPreferences settings = getSharedPreferences(getString(R.string.sharedprefs), 0);
        if(settings.getBoolean(getString(R.string.first_launch), true)) {
            FirstLaunchFragment firstLaunchFragment = new FirstLaunchFragment();
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, firstLaunchFragment).commitAllowingStateLoss();
        } else {
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        }


    }
}



