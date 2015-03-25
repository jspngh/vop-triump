package be.ugent.vop.ui.event;

import android.os.Bundle;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;

/**
 * Created by vincent on 24/03/15.
 */
public class NewEventActivity extends BaseActivity {

    public static final String VENUE_ID = "venueID";

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        NewEventFragment fragment = new NewEventFragment();
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