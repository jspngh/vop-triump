package be.ugent.vop.feedback;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.ui.main.MainActivity;

/**
 * Created by vincent on 11/04/15.
 */
public class FeedbackActivity extends BaseActivity {
    private static final String TAG = "FeedbackActivity";

    Feedback fb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        fb = new Feedback(this);
        fb.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume FeedbackActivity");
        fb = new Feedback(this);
        fb.show();
        /*


        if(!fb.isFeedbackGiven()) fb.show();
        else {
            Intent intent= new Intent(this, MainActivity.class);
            startActivity(intent);
        }*/

    }

    @Override
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_FEEDBACK;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


}
