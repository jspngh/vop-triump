package be.ugent.vop.ui.venue;

import android.os.Bundle;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;

/**
 * Created by vincent on 03/03/15.
 */
public class VenueActivity extends BaseActivity {

    public static final String VENUE_ID = "venueID";

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
