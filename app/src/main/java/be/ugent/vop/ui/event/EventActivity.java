package be.ugent.vop.ui.event;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;




import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.feedback.Feedback;
import be.ugent.vop.feedback.FeedbackDialog;

/**
 * Created by siebe on 25/02/15.
 */
public class EventActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

      EventFragment fragment = new EventFragment();
        Bundle venueBundle = getIntent().getExtras();

        fragment.setArguments(venueBundle);

        this.getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();


    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_EVENT;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
