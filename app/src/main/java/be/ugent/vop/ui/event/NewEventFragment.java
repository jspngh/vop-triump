package be.ugent.vop.ui.event;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
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
import android.widget.TextView;
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
import be.ugent.vop.backend.loaders.EventLoader;
import be.ugent.vop.backend.loaders.GroupsForUserLoader;
import be.ugent.vop.backend.loaders.RankingLoader;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.GroupsBean;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.ui.group.GroupActivity;
import be.ugent.vop.ui.venue.RankingAdapter;
import be.ugent.vop.ui.venue.VenueActivity;
import be.ugent.vop.utils.RangeSeekBar;

/**
 * Created by vincent on 23/03/15.
 */
public class NewEventFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "EventFragment";
    private static final int MIN_PARTICIPANTS = 1;
    private static final int MAX_PARTICIPANTS = 1000;

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
    String startHourMinute = "00:00";
    String endHourMinute = "00:00";
    Date startDate = new Date();
    Date endDate = new Date();
    DateTime start;
    DateTime end;

    RangeSeekBar<Integer> seekBar;
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

        // create RangeSeekBar as Integer range between Min- and Max participants
        seekBar = new RangeSeekBar<Integer>(MIN_PARTICIPANTS, MAX_PARTICIPANTS, getActivity());
        // add RangeSeekBar to pre-defined layout
        ViewGroup layout = (ViewGroup) rootView.findViewById(R.id.viewGroup);
        layout.addView(seekBar);

        initButtonPressedLogic();

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
     * new event loader
     */


    private LoaderManager.LoaderCallbacks<EventBean> newEventLoader
            = new LoaderManager.LoaderCallbacks<EventBean>() {

        @Override
        public void onLoadFinished(Loader<EventBean> loader, EventBean event) {
            Log.d(TAG, "onLoadFinished, NewEventLoader");

            if(event != null) {
                   Toast.makeText(getActivity(),"Succes",Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public Loader<EventBean> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            NewEventLoader loader = new NewEventLoader(getActivity());
            loader.setParams(fsVenueId,groupIds,start,end,description,reward,minMembers,maxMembers,verified);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<EventBean> loader) {
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
        boolean correctSelectedGroupsInput = true;
        boolean correctTextInput = true;

        description = descriptionEditText.getText().toString();
        reward = rewardEditText.getText().toString();

        try {
            minMembers = Integer.parseInt(minEditText.getText().toString());
            maxMembers = Integer.parseInt(maxEditText.getText().toString());
        }catch(NumberFormatException e){
            minMembers = -1;
            maxMembers = -1;
        }


        procesDates();
        start = new DateTime(startDate);
        end = new DateTime(endDate);

       if(!(correctDatesInput=correctDates())){
            Log.d(TAG, "Incorrect date and time");
            //startDateEditText.setHintTextColor(some color);
        }
        if(!(correctSizeInput=correctGroupSize())){
            Log.d(TAG, "Incorrect input in applicable groupsizes, min and max members");
            //startDateEditText.setHintTextColor(some color);
        }
        if(!(correctSelectedGroupsInput=correctSelectedGroups())){
            Log.d(TAG, "Incorrect input, no groups selected (only for non verified events)");
            //startDateEditText.setHintTextColor(some color);
        }
        if(!(correctTextInput=correctTextInput())){
            Log.d(TAG, "Incorrect description or reward");
            //startDateEditText.setHintTextColor(some color);
        }
        if(correctDatesInput && correctSizeInput && correctSelectedGroupsInput && correctTextInput ){

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
            for(GroupBean g:selectedGroups){
                groupIds.add(g.getGroupId());
            }
            //init loader
            getLoaderManager().initLoader(1, null, newEventLoader);

        }
    }
    /**
     *
     * Check user input
     *
     */
    private boolean correctDates(){
        return startDate.before(endDate);
    }
    private boolean correctGroupSize(){
        return minMembers>=MIN_PARTICIPANTS && minMembers<maxMembers && maxMembers<=MAX_PARTICIPANTS;
    }
    private boolean correctSelectedGroups(){
        if(!verified){
            return selectedGroups.size()>0;
        }
        //when the event is verified the selected groups don't matter.
        //all the groups can participate
        else return true;
    }
    private boolean correctTextInput(){
        //TODO: check user input for event description and event reward
        return description.length()>0 && reward.length()>0;
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

    public void initButtonPressedLogic(){
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

        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                // handle changed range values
                minEditText.setText(""+minValue);
                maxEditText.setText(""+maxValue);
            }
        });


        minEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int min,max;
                try {
                    min = Integer.parseInt(minEditText.getText().toString());
                } catch(NumberFormatException e){
                    min = MIN_PARTICIPANTS;
                }
                try {
                    max = Integer.parseInt(maxEditText.getText().toString());
                } catch(NumberFormatException e){
                    max = MAX_PARTICIPANTS;
                }
                if(min<MIN_PARTICIPANTS) min = MIN_PARTICIPANTS;
                if(min>max) min=max;
                seekBar.setSelectedMinValue(min);
           }
        });

        maxEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int min,max;
                try {
                    min = Integer.parseInt(minEditText.getText().toString());
                } catch(NumberFormatException e){
                    min = MIN_PARTICIPANTS;
                }
                try {
                    max = Integer.parseInt(maxEditText.getText().toString());
                } catch(NumberFormatException e){
                    max = MAX_PARTICIPANTS;
                }
                if(max>MAX_PARTICIPANTS) max=MAX_PARTICIPANTS;
                if(max<min) max=min;
                seekBar.setSelectedMaxValue(max);
            }
        });
    }
}
