package be.ugent.vop.ui.event;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;

import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.loaders.GroupsForUserLoader;
import be.ugent.vop.backend.loaders.RankingLoader;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.GroupsBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.ui.group.GroupActivity;
import be.ugent.vop.ui.venue.RankingAdapter;
import be.ugent.vop.ui.venue.VenueActivity;

/**
 * Created by vincent on 23/03/15.
 */
public class NewEventFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "EventFragment";

    String fsVenueId;

    Button createButton;
    Button selectGroupsButton;

    /*
     * TODO: Add hour and minute to start- and end date!
     */

    EditText descriptionEditText, rewardEditText;
    EditText startDateEditText, startTimeEditText;
    EditText endDateEditText, endTimeEditText;
    EditText minEditText, maxEditText;

    DatePickerDialog startDateDialog;
    DatePickerDialog endDateDialog;
    TimePickerDialog startTimeDialog;
    TimePickerDialog endTimeDialog;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    int startYear=-1, startDay=-1, startMonth=-1;
    String startHourMinute;
    String endHourMinute;
    Date startDate = null;
    Date endDate = null;
    DateTime start;
    DateTime end;

    int minMembers = -1;
    int maxMembers = -1;

    CheckBox verifiedCheckBox;
    Boolean verified = true;

    SelectGroupsDialog dialog;
    FragmentManager fm;

    List<Long> groupIds = new ArrayList<Long>();
    List<GroupBean> userGroups = null;
    List<GroupBean> selectedGroups = new ArrayList<>();

    String description ="", reward="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_new_event, container, false);

        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getArguments().getString(VenueActivity.VENUE_ID);

        createButton = (Button) rootView.findViewById(R.id.buttonCreateEvent);
        selectGroupsButton = (Button) rootView.findViewById(R.id.buttonSelectGroups);

        descriptionEditText = (EditText) rootView.findViewById(R.id.editTextDescription);
        rewardEditText = (EditText) rootView.findViewById(R.id.editTextReward);

        dialog = new SelectGroupsDialog();
        dialog.setContext(getActivity());
        fm = getFragmentManager();

        //init loader
        getLoaderManager().initLoader(0, null, groupLoaderListener);

        //date
        startDateEditText = (EditText) rootView.findViewById(R.id.editTextStartDate);
        startTimeEditText = (EditText) rootView.findViewById(R.id.editTextStartTime);
        endDateEditText = (EditText) rootView.findViewById(R.id.editTextEndDate);
        endTimeEditText = (EditText) rootView.findViewById(R.id.editTextEndTime);
        initDateTimeDialog();

        minEditText = (EditText) rootView.findViewById(R.id.editTextMin);
        maxEditText = (EditText) rootView.findViewById(R.id.editTextMax);

        //verified
        verifiedCheckBox = (CheckBox) rootView.findViewById(R.id.checkBoxVerified);
        verifiedCheckBox.setOnClickListener(this);

        selectGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectGroupDialog();
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createButtonPressed();
            }
        });
        return rootView;
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch(view.getId()) {
            //verified venue
            case R.id.checkBoxVerified:
                verified = !verified;
                break;
        }
    }


    /**
     *
     * Check user input
     *
     */
    private boolean correctDates(){

        return false;
    }
    private boolean correctGroupSize(){
        //incorrect if no item are checked
        //   return (sizeAll || sizeIndividual|| sizeSmall|| sizeMedium|| sizeLarge);
        return true;
    }
    private boolean correctGroupTypes(){
        //incorrect if no item are checked
        //    return (typeAll|| typeFriends|| typeClub|| typeStudentGroup);
        return true;
    }
    private boolean correctTextInput(){
        //TODO: check user input for event description and event reward
        return true;
    }

    /**
     *
     * Date and time on click listeners initializing
     */

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
                        String date = dateFormatter.format(newDate.getTime());
                        try {
                            startDate = dateFormatter.parse(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        startDateEditText.setText(date);
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

                        startHourMinute = ""+((hour<9)? "0"+hour:hour)+":"+(""+((minute<9)?"0"+minute:minute));
                        startTimeEditText.setText(startHourMinute);

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
                        String date = dateFormatter.format(newDate.getTime());
                        try {
                            endDate = dateFormatter.parse(date);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        endDateEditText.setText(date);
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
                        endHourMinute = ""+((hour<9)? "0"+hour:hour)+":"+(""+((minute<9)?"0"+minute:minute));
                        endTimeEditText.setText(endHourMinute);

                    }

                },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

                endTimeDialog.show();

            }
        });
    }


    /**
     *
     * User group loader
     */


    private LoaderManager.LoaderCallbacks<GroupsBean> groupLoaderListener
            = new LoaderManager.LoaderCallbacks<GroupsBean>() {

        @Override
        public void onLoadFinished(Loader<GroupsBean> loader, GroupsBean groups) {
            Log.d(TAG, "onLoadFinished");
            /**************************************
             Resultaat kan null zijn
             Rekening mee houden!
             **************************************/
            if(groups != null && groups.getGroups() != null) {
                userGroups = groups.getGroups();
                dialog.setGroups(userGroups);
            }
        }

        @Override
        public Loader<GroupsBean> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            return new GroupsForUserLoader(getActivity());
        }

        @Override
        public void onLoaderReset(Loader<GroupsBean> loader) {
            //rankingListView.setAdapter(null);
        }
    };



    /**
     *
     * Logic if buttons are pressed
     */

    private void showSelectGroupDialog() {
        selectedGroups = new ArrayList<>();
        dialog.setSelectedGroups(selectedGroups);
        dialog.show(fm, "dialog");
    }

    private void createButtonPressed(){
        boolean correctDatesInput = true;
        boolean correctSizeInput = true;
        boolean correctTypeInput = true;
        boolean correctTextInput = true;

        description = descriptionEditText.getText().toString();
        reward = rewardEditText.getText().toString();
        minMembers = Integer.parseInt(minEditText.getText().toString());
        maxMembers = Integer.parseInt(maxEditText.getText().toString());

        procesDates();
        start = new DateTime(startDate);
        end = new DateTime(endDate);

              /*  if(!(correctDatesInput=correctDates())){
                    Log.d(TAG, "Incorrect date and time");
                    //startDateEditText.setHintTextColor(some color);
                }
                if(!(correctSizeInput=correctGroupSize())){
                    Log.d(TAG, "Incorrect checkbox input in groupsizes");
                    //startDateEditText.setHintTextColor(some color);
                }
                if(!(correctTypeInput=correctGroupTypes())){
                    Log.d(TAG, "Incorrect checkbox input in grouptypes");
                    //startDateEditText.setHintTextColor(some color);
                }
                if(!(correctTextInput=correctTextInput())){
                    Log.d(TAG, "Incorrect description or reward");
                    //startDateEditText.setHintTextColor(some color);
                }*/
        if(correctDatesInput && correctSizeInput && correctTypeInput && correctTextInput ){

                Log.d(TAG, "startDate:"+start.toString());
                Log.d(TAG, "endDate:"+end.toString());
                Log.d(TAG, "des: "+description);
                Log.d(TAG, "reward: "+reward);
                Log.d(TAG, "venueId: "+fsVenueId);
                Log.d(TAG, "min: "+minMembers);
                Log.d(TAG, "max: "+maxMembers);
                for(GroupBean g:selectedGroups){
                    Log.d(TAG, "group: "+g.toString());
                }



        }
    }

    public void procesDates(){
        String date = dateFormatter.format(startDate.getTime())+" "+startHourMinute;
        try {
          startDate = dateTimeFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        date = dateFormatter.format(endDate.getTime())+" "+endHourMinute;
        try {
            endDate = dateTimeFormatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
