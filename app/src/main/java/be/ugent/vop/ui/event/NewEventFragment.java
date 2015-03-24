package be.ugent.vop.ui.event;

import android.app.Fragment;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Date;

import be.ugent.vop.R;

/**
 * Created by vincent on 23/03/15.
 */
public class NewEventFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "EventFragment";

    Button createButton;
    DatePicker startDatePicker, endDatePicker;
    TimePicker startTimePicker, endTimePicker;
    EditText descriptionEditText, rewardEditText;
    EditText startDateEditText, startTimeEditText;
    EditText endDateEditText, endTimeEditText;

    CheckBox verifiedCheckBox;
    CheckBox typeAllCheckBox, typeFriendsCheckBox, typeClubCheckBox, typeStudentGroupCheckBox;
    CheckBox sizeAllCheckBox, sizeIndividualCheckBox, sizeSmallCheckBox, sizeMediumCheckBox, sizeLargeCheckBox;
    Boolean typeAll= false, typeFriends= false, typeClub= false, typeStudentGroup = false;
    Boolean sizeAll= false, sizeIndividual= false, sizeSmall= false, sizeMedium= false, sizeLarge= false;
    Boolean verified;

    String venueId;

    String description ="", reward="";
    Date startDate;
    Date EndDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_new_event, container, false);

        createButton = (Button) rootView.findViewById(R.id.buttonCreateEvent);

        descriptionEditText = (EditText) rootView.findViewById(R.id.editTextDescription);
        rewardEditText = (EditText) rootView.findViewById(R.id.editTextReward);

        //date
        /*
        startDatePicker = (DatePicker) rootView.findViewById(R.id.datePickerStart);
        startTimePicker = (TimePicker) rootView.findViewById(R.id.timePickerStart);
        endDatePicker = (DatePicker) rootView.findViewById(R.id.datePickerEnd);
        endTimePicker = (TimePicker) rootView.findViewById(R.id.timePickerEnd);*/
        startDateEditText = (EditText) rootView.findViewById(R.id.editTextStartDate);
        startTimeEditText = (EditText) rootView.findViewById(R.id.editTextStartTime);
        endDateEditText = (EditText) rootView.findViewById(R.id.editTextEndDate);
        endTimeEditText = (EditText) rootView.findViewById(R.id.editTextEndTime);
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // DialogFragment newFragment = new TimePickerFragment();
                // newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        //type checkbox
        typeAllCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeAll);
        typeFriendsCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeFriends);
        typeClubCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeClub);
        typeStudentGroupCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeStudentGroup);
        typeAllCheckBox.setOnClickListener(this);
        typeFriendsCheckBox.setOnClickListener(this);
        typeClubCheckBox.setOnClickListener(this);
        typeStudentGroupCheckBox.setOnClickListener(this);
        //size checkbox
        sizeAllCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeAll);
        sizeIndividualCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeIndividual);
        sizeSmallCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeSmall);
        sizeMediumCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeMedium);
        sizeLargeCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeLarge);
        sizeAllCheckBox.setOnClickListener(this);
        sizeIndividualCheckBox.setOnClickListener(this);
        sizeSmallCheckBox.setOnClickListener(this);
        sizeMediumCheckBox.setOnClickListener(this);
        sizeLargeCheckBox.setOnClickListener(this);
        //verified
        verifiedCheckBox = (CheckBox) rootView.findViewById(R.id.checkBoxVerified);
        verifiedCheckBox.setOnClickListener(this);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                description = descriptionEditText.getText().toString();
                reward = rewardEditText.getText().toString();


                Log.d(TAG,"description: "+description);
                Log.d(TAG,"reward: "+reward);

            }
        });


        return rootView;
    }

    /*

    Check user input
     */
    private boolean checkDates(String startDate, String endDate){
        boolean flag = false;

        return flag;
    }

    @Override
    public void onClick(View view) {



        // Check which checkbox was clicked
        switch(view.getId()) {
            //size
            case R.id.checkboxSizeAll:
                    sizeAll = !sizeAll;
                break;
            case R.id.checkboxSizeIndividual:
                sizeIndividual = !sizeIndividual;
                break;
            case R.id.checkboxSizeSmall:
               sizeSmall = !sizeSmall;
                break;
            case R.id.checkboxSizeMedium:
                sizeMedium = !sizeMedium;
                break;
            case R.id.checkboxSizeLarge:
                sizeLarge = !sizeLarge;
                break;
           //type
            case R.id.checkboxTypeAll:
                typeAll = !typeAll;
                break;
            case R.id.checkboxTypeClub:
                typeClub = !typeClub;
                break;
            case R.id.checkboxTypeFriends:
                typeFriends = !typeFriends;
                break;
            case R.id.checkboxTypeStudentGroup:
                typeStudentGroup = !typeStudentGroup;
                break;

            //verified venue
            case R.id.checkBoxVerified:
                verified = !verified;
                break;
        }
    }

}
