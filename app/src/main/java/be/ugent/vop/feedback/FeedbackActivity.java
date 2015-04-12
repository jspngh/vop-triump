package be.ugent.vop.feedback;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;

/**
 * Created by vincent on 11/04/15.
 */
public class FeedbackActivity extends BaseActivity {
    private static final String TAG = "FeedbackActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Feedback fb = new Feedback(this);
        fb.show();

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
