package be.ugent.vop.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import be.ugent.vop.R;
import be.ugent.vop.ui.main.MainActivity;
import be.ugent.vop.utils.PrefUtils;

public class LoginActivity extends Activity implements LoginFragment.OnFragmentInteractionListener{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);
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
            // Create a new Fragment to be placed in the activity layout
            LoginFragment loginFragment = new LoginFragment();
            loginFragment.setArguments(mBundle);
            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, loginFragment).commit();

/*              ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(mBundle);
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, profileFragment).commit();*/

        }
    }

    public void onLoginFragmentInteraction(){
        if(PrefUtils.isFirstLaunch(this)) {
            Intent main = new Intent(this, FirstLaunchActivity.class);
            startActivity(main);
            this.finish();
        } else {
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        }
    }
}



