package be.ugent.vop.ui.venue;

import android.os.Bundle;

import com.facebook.Session;
import com.facebook.SessionState;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.ui.venue.VenueFragment;

/**
 * Created by vincent on 03/03/15.
 */
public class VenueActivity extends BaseActivity {

    @Override
    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {

    }

    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_venue);

    VenueFragment fragment = new VenueFragment();
    Bundle venueBundle = getIntent().getExtras();

    fragment.setArguments(venueBundle);

   this.getFragmentManager()
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_OTHER;
    }
}
