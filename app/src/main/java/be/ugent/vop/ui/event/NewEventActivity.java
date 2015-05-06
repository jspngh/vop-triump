package be.ugent.vop.ui.event;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;
import com.google.api.client.util.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.GroupsForUserLoader;
import be.ugent.vop.backend.loaders.NewEventLoader;
import be.ugent.vop.backend.myApi.model.EventBean;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.backend.myApi.model.GroupsBean;
import be.ugent.vop.ui.venue.VenueActivity;
import be.ugent.vop.utils.PrefUtils;


public class NewEventActivity extends ActionBarActivity implements SelectGroupsDialog.SelectGroupsDialogListener {
    private static final String TAG = "NewEventActivity";
    public static final String VENUE_ID = "venueID";

    private Activity mContext;
    private Toolbar mActionBarToolbar;
    private String fsVenueId;


    private EditText mName;
    private EditText mReward;
    private EditText mStartDate;
    private EditText mStartTime;
    private EditText mEndDate;
    private EditText mEndTime;
    private EditText mSelectGroups;
    private ButtonRectangle mCreateButton;

    private SelectGroupsDialog mDialog;
    private Date mStart = new Date();
    private Date mEnd = new Date();

    private List<Long> groupIds = new ArrayList<Long>();
    private List<GroupBean> selectedGroups = new ArrayList<>();

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
    private String description;
    private String reward;

    public void onCreate(Bundle savedInstanceState){
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        getActionBarToolbar();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(null);
        }

        mContext = this;

        setTitle(getString(R.string.new_event_title));

        if(getIntent().getExtras().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getIntent().getExtras().getString(VenueActivity.VENUE_ID);

        /* Find views */
        mName = (EditText) findViewById(R.id.new_event_name);
        mReward = (EditText) findViewById(R.id.new_event_reward);
        mStartDate = (EditText) findViewById(R.id.new_event_start_date);
        mStartTime = (EditText) findViewById(R.id.new_event_start_time);
        mEndDate = (EditText) findViewById(R.id.new_event_end_date);
        mEndTime = (EditText) findViewById(R.id.new_event_end_time);
        mSelectGroups = (EditText) findViewById(R.id.new_event_select_groups);
        mCreateButton = (ButtonRectangle) findViewById(R.id.buttonCreateEvent);


        mDialog = new SelectGroupsDialog();
        mDialog.setSelectGroupsDialogListener(this);

        initDateTimeDialog();
        initButtonPressedLogic();

        //init loader
        getLoaderManager().initLoader(0, null, groupLoaderListener);
    }

    private Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    /**
     *
     * Date and time on click listeners initializing
     */

    private void initDateTimeDialog() {

        mStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();

                DatePickerDialog startDateDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        mStart = newDate.getTime();
                        String date = dateFormatter.format(newDate.getTime());
                        mStartDate.setText(date);
                    }

                },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
                startDateDialog.show();
            }
        });


        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();

                TimePickerDialog startTimeDialog = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        Calendar newTime = Calendar.getInstance();
                        newTime.setTime(mStart);
                        newTime.set(Calendar.HOUR_OF_DAY, hour);
                        newTime.set(Calendar.MINUTE, minute);
                        newTime.set(Calendar.SECOND, 0);
                        newTime.set(Calendar.MILLISECOND, 0);
                        mStart = newTime.getTime();

                        mStartTime.setText(timeFormatter.format(mStart));
                    }

                },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

                startTimeDialog.show();
            }
        });

        mEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();
                newCalendar.setTime(mStart);

                int day = newCalendar.get(Calendar.DAY_OF_MONTH);
                int month =newCalendar.get(Calendar.MONTH);
                int year = newCalendar.get(Calendar.YEAR);

                DatePickerDialog endDateDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        mEnd = newDate.getTime();

                        String date = dateFormatter.format(mEnd);
                        mEndDate.setText(date);
                    }

                },year,month,day);

                endDateDialog.show();
            }
        });

        mEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar newCalendar = Calendar.getInstance();

                TimePickerDialog endTimeDialog = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener(){
                    public void onTimeSet(TimePicker view, int hour, int minute) {
                        Calendar newTime = Calendar.getInstance();
                        newTime.setTime(mEnd);
                        newTime.set(Calendar.HOUR_OF_DAY, hour);
                        newTime.set(Calendar.MINUTE, minute);
                        newTime.set(Calendar.SECOND, 0);
                        newTime.set(Calendar.MILLISECOND, 0);
                        mEnd = newTime.getTime();

                        mEndTime.setText(timeFormatter.format(mEnd));

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
                mDialog.setGroups(groups.getGroups());
            }
        }

        @Override
        public Loader<GroupsBean> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            return new GroupsForUserLoader(mContext);
        }

        @Override
        public void onLoaderReset(Loader<GroupsBean> loader) {
            mDialog.setGroups(null);
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
                Toast.makeText(mContext, mContext.getString(R.string.new_event_succes), Toast.LENGTH_SHORT).show();
                mContext.finish();
            }else{
                Toast.makeText(mContext, mContext.getString(R.string.new_event_failed),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public Loader<EventBean> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            NewEventLoader loader = new NewEventLoader(mContext);
            loader.setParams(fsVenueId, groupIds, new DateTime(mStart), new DateTime(mEnd), description, reward, -1, -1, false);
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
        mDialog.show(getFragmentManager(), "dialog");
    }


    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        selectedGroups = ((SelectGroupsDialog) dialog).getSelectedGroups();
        String s = "";
        for(int i = 0;i<selectedGroups.size();i++){
            if(i==selectedGroups.size()) s+=selectedGroups.get(i).getName();
            else s+= selectedGroups.get(i).getName()+"\n";
        }
        mSelectGroups.setText(s);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) { /* Do Nothing */ }



    private void createButtonPressed(){
        boolean correctDatesInput;
        boolean correctSelectedGroupsInput;
        boolean correctTextInput;

        description = mName.getText().toString();
        reward = mReward.getText().toString();

        if(!(correctDatesInput=correctDates())){
            Log.d(TAG, "Incorrect date and time");
            mStartDate.setError(mContext.getString(R.string.incorrect_date));
        }
        if(!(correctSelectedGroupsInput=correctSelectedGroups())){
            Log.d(TAG, "Incorrect input, no groups selected (only for non verified events)");
            mSelectGroups.setError(mContext.getString(R.string.incorrect_groups));
        }
        if(!(correctTextInput=correctTextInput())){
            Log.d(TAG, "Incorrect description or reward");
            mName.setError(mContext.getString(R.string.incorrect_name));
        }

        if(correctDatesInput && correctSelectedGroupsInput && correctTextInput ){

            Log.d(TAG, "startDate:" + mStart.toString());
            Log.d(TAG, "endDate:" + mEnd.toString());
            Log.d(TAG, "des: " + description);
            Log.d(TAG, "reward: " + reward);
            Log.d(TAG, "venueId: " + fsVenueId);

            for(GroupBean g:selectedGroups){
                Log.d(TAG, "group: " + g.toString());
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
        return mStart.before(mEnd);
    }

    private boolean correctSelectedGroups(){
        return selectedGroups.size()>0;
    }

    private boolean correctTextInput(){
        //TODO: check user input for event description and event reward
        return mName.length()>0 && mReward.length()>0;
    }

    public void initButtonPressedLogic(){

        mSelectGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectGroupDialog();
            }
        });

        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createButtonPressed();
            }
        });
    }
}
