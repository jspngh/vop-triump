package be.ugent.vop.ui.event;

import android.os.Bundle;


import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.utils.PrefUtils;

/**
 * Created by siebe on 25/02/15.
 */
public class EventActivity extends BaseActivity {
    private static final String FRAGMENT_LIST_VIEW = "list view";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new EventListViewFragment(), FRAGMENT_LIST_VIEW)
                    .commit();
        }

    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_EVENT;
    }
}
