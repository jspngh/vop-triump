package be.ugent.vop.feedback;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import be.ugent.vop.BaseActivity;
import be.ugent.vop.R;
import be.ugent.vop.ui.main.MainActivity;

/**
 * Created by vincent on 11/04/15.
 */
public class Feedback implements FeedbackDialog.FeedBackDialogListener {
    private static final String TAG = "Feedback";

    //string used in intent, to start feedback after notification
    public static final String GIVE_FEEDBACK = "Feedback";

    private FeedbackItem feedback;

    private boolean mailSend = false;

    private static final String[] recipients = new String[]{
            "dutordoirv@gmail.com"
            //, "triumpapplication@gmail.com"
            //, ...
    };

    private ActionBarActivity activity;

    public Feedback(BaseActivity activity){
        this.activity = activity;
    }

    public boolean isFeedbackGiven(){
        return mailSend;
    }

    public void show(){
        FeedbackDialog newFragment = new FeedbackDialog();
        newFragment.setFeedBackDialogListener(this);
        newFragment.show(activity.getSupportFragmentManager(),null);

    }

    /*
    * Interface overrides
     */

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // save feedbackitem
        this.feedback = ((FeedbackDialog) dialog).getFeedback();
        Log.d(TAG, "feedback message: " + feedback.getMessage());
        Log.d(TAG, "feedback type: "+feedback.getType());

        if(feedback!=null){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL  , recipients);
            intent.putExtra(Intent.EXTRA_SUBJECT, feedback.getType());
            intent.putExtra(Intent.EXTRA_TEXT   , feedback.getMessage());
            try {
                activity.startActivity(
                        Intent.createChooser(intent,
                                activity.getString(R.string.feedback_dialog_send_mail)));
                mailSend = true;
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(activity,
                        activity.getString(R.string.feedback_dialog_no_mail_clients),
                        Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Intent intent= new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
    }


}
