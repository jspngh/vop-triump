package be.ugent.vop.ui.event;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import be.ugent.vop.R;

/**
 * Created by vincent on 23/03/15.
 */
public class NewEventFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "EventFragment";

    Button createButton;

    EditText descriptionEditText, rewardEditText;
    EditText startDateEditText, startTimeEditText;
    EditText endDateEditText, endTimeEditText;

    DatePickerDialog startDateDialog;
    DatePickerDialog endDateDialog;
    TimePickerDialog startTimeDialog;
    TimePickerDialog endTimeDialog;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    int startYear=-1, startDay=-1, startMonth=-1;

    CheckBox verifiedCheckBox;
    CheckBox typeAllCheckBox, typeFriendsCheckBox, typeClubCheckBox, typeStudentGroupCheckBox;
    CheckBox sizeAllCheckBox, sizeIndividualCheckBox, sizeSmallCheckBox, sizeMediumCheckBox, sizeLargeCheckBox;
    Boolean typeAll= false, typeFriends= false, typeClub= false, typeStudentGroup = false;
    Boolean sizeAll= false, sizeIndividual= false, sizeSmall= false, sizeMedium= false, sizeLarge= false;
    Boolean verified = false;

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
        startDateEditText = (EditText) rootView.findViewById(R.id.editTextStartDate);
        startTimeEditText = (EditText) rootView.findViewById(R.id.editTextStartTime);
        endDateEditText = (EditText) rootView.findViewById(R.id.editTextEndDate);
        endTimeEditText = (EditText) rootView.findViewById(R.id.editTextEndTime);
        initDateTimeDialog();


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

    private void initDateTimeDialog() {
        startDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();

                startDateDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        startYear = year;
                        startMonth = monthOfYear;
                        startDay= dayOfMonth;
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        startDateEditText.setText(dateFormatter.format(newDate.getTime()));
                    }

                },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                startDateDialog.show();
            }
        });


        startTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();

                startTimeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        //   Calendar newTime = Calendar.getInstance();
                        //   newDate.set(year, monthOfYear, dayOfMonth);
                        startTimeEditText.setText(hour+":"+minute+":00");
                    }

                },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

                startTimeDialog.show();

            }
        });

        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();

                int day = newCalendar.get(Calendar.DAY_OF_MONTH);
                int month =newCalendar.get(Calendar.MONTH);
                int year = newCalendar.get(Calendar.YEAR);

                //initialize day, month and year to startDate
                if(startDay!=-1 && startYear!=-1 && startMonth!=-1){
                    day = startDay;
                    month = startMonth;
                    year = startYear;
                }
                endDateDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        endDateEditText.setText(dateFormatter.format(newDate.getTime()));
                    }

                },year,month,day);

                endDateDialog.show();
            }
        });

        endTimeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();

                endTimeDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        //   Calendar newTime = Calendar.getInstance();
                        //   newDate.set(year, monthOfYear, dayOfMonth);
                        endTimeEditText.setText(hour+":"+minute+":00");
                    }

                },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

                endTimeDialog.show();

            }
        });
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
