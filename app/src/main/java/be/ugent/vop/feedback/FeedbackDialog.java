package be.ugent.vop.feedback;




import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;


import android.view.LayoutInflater;


import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import be.ugent.vop.R;

/**
 * Created by vincent on 11/04/15.
 */
public class FeedbackDialog extends DialogFragment {

    //views
    private EditText commentET;
    private RadioGroup typeRG;

    private FeedbackItem feedback= new FeedbackItem();

    // Use this instance of the interface to deliver action events
    private FeedBackDialogListener mListener;

    public void setFeedBackDialogListener(FeedBackDialogListener list){
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (FeedBackDialogListener) list;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(list.toString()
                    + " must implement NoticeDialogListener");
        }
    }


    public FeedbackItem getFeedback() {
        return feedback;
    }



    /* The activity that creates an instance of this dialog fragment must
        * implement this interface in order to receive event callbacks.
        * Each method passes the DialogFragment in case the host needs to query it. */
    public interface FeedBackDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_feedback, null);
        commentET = (EditText) rootView.findViewById(R.id.editTextComment);
        typeRG = (RadioGroup) rootView.findViewById(R.id.radioGroupFeedbackType);

        //set radiobutton "bug" default as checked
        ((RadioButton) rootView.findViewById(R.id.radioButtonBug)).setChecked(true);
        feedback.setType(FeedbackItem.TYPE_BUG);

        initRadioGroup();

       final  AlertDialog dialog = builder.setTitle(R.string.feedback_dialog_title)
                .setMessage(getString(R.string.feedback_dialog_message))
                .setView(rootView)
                .setPositiveButton(R.string.button_text_send , null) //Set to null. We override the onclick
                .setNegativeButton(R.string.button_text_cancel,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(FeedbackDialog.this);
                    }
                })
                .create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface d) {
                        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                if (commentET.getText().toString().matches("")) {
                                    Toast.makeText(getActivity(),
                                            getString(R.string.feedback_dialog_no_comment_toast),
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                feedback.setMessage(commentET.getText().toString());
                                mListener.onDialogPositiveClick(FeedbackDialog.this);
                                //Dismiss once everything is OK.
                                dialog.dismiss();
                            }
                        });
                    }
                });

        return dialog;
    }

    private void initRadioGroup() {

        typeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override

            public void onCheckedChanged(RadioGroup group, int checkedId) {
                   switch(checkedId){
                       case R.id.radioButtonBug:
                           feedback.setType(FeedbackItem.TYPE_BUG);
                           break;
                       case R.id.radioButtonIdea:
                           feedback.setType(FeedbackItem.TYPE_IDEA);
                           break;
                       case R.id.radioButtonOther:
                           feedback.setType(FeedbackItem.TYPE_OTHER);
                           break;
                   }
            }

        });

    }


}
