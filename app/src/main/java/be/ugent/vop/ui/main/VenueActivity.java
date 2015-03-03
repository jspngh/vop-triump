package be.ugent.vop.ui.main;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.VenueFragment;

/**
 * Created by vincent on 03/03/15.
 */
public class VenueActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState){
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_venue);

    VenueFragment fragment = new VenueFragment();
    Bundle venueBundle = getIntent().getExtras();

    fragment.setArguments(venueBundle);

   this.getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.main_content, fragment)
            .commit();
    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_OTHER;
    }
}
