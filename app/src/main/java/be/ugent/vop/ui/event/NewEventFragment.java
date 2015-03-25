package be.ugent.vop.ui.event;

import android.app.DatePickerDialog;
import android.app.Fragment;
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

    NewEventGroupListAdapter adapter;

    /*
     * TODO: Add hour and minute to start- and end date!
     */

    EditText descriptionEditText, rewardEditText;
    EditText startDateEditText, startTimeEditText;
    EditText endDateEditText, endTimeEditText;
    ListView groupsListView;

    DatePickerDialog startDateDialog;
    DatePickerDialog endDateDialog;
    TimePickerDialog startTimeDialog;
    TimePickerDialog endTimeDialog;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
    int startYear=-1, startDay=-1, startMonth=-1;
    int startHour, startMinute;
    int endHour, endMinute;
    Date startDate = null;
    Date endDate = null;
    DateTime start;
    DateTime end;

    CheckBox verifiedCheckBox;
    CheckBox typeFriendsCheckBox, typeClubCheckBox, typeStudentGroupCheckBox;
    CheckBox sizeIndividualCheckBox, sizeSmallCheckBox, sizeMediumCheckBox, sizeLargeCheckBox;
    Boolean typeAll= false, typeFriends= false, typeClub= false, typeStudentGroup = false;
    Boolean sizeAll= false, sizeIndividual= false, sizeSmall= false, sizeMedium= false, sizeLarge= false;
    List<String> types = new ArrayList<>();
    List<String> sizes = new ArrayList<>();
    Boolean verified = false;

    List<Long> groupIds = new ArrayList<Long>();

    String description ="", reward="";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_new_event, container, false);

        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getArguments().getString(VenueActivity.VENUE_ID);

        createButton = (Button) rootView.findViewById(R.id.buttonCreateEvent);

        descriptionEditText = (EditText) rootView.findViewById(R.id.editTextDescription);
        rewardEditText = (EditText) rootView.findViewById(R.id.editTextReward);
        groupsListView = (ListView) rootView.findViewById(R.id.listViewSelectGroups);

        groupsListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getLoaderManager().initLoader(0, null, groupLoaderListener);

        groupsListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view,int position,long id) {
              //  View v = groupsListView.getChildAt(position);
                CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.checkedTextViewGroup);

                Log.d("TAG","GroupName: "+ adapter.getItem(position).getName());
                if(!ctv.isChecked()){
                    ctv.setChecked(true);
                groupIds.add(adapter.getItem(position).getGroupId());
                }else{
                    ctv.setChecked(false);
                    groupIds.remove(adapter.getItem(position).getGroupId());
                }
            }
        });

        //date
        startDateEditText = (EditText) rootView.findViewById(R.id.editTextStartDate);
        startTimeEditText = (EditText) rootView.findViewById(R.id.editTextStartTime);
        endDateEditText = (EditText) rootView.findViewById(R.id.editTextEndDate);
        endTimeEditText = (EditText) rootView.findViewById(R.id.editTextEndTime);
        initDateTimeDialog();


        //type checkbox
        //typeAllCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeAll);
        typeFriendsCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeFriends);
        typeClubCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeClub);
        typeStudentGroupCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxTypeStudentGroup);
        //typeAllCheckBox.setOnClickListener(this);
        typeFriendsCheckBox.setOnClickListener(this);
        typeClubCheckBox.setOnClickListener(this);
        typeStudentGroupCheckBox.setOnClickListener(this);
        //size checkbox
        //sizeAllCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeAll);
        sizeIndividualCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeIndividual);
        sizeSmallCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeSmall);
        sizeMediumCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeMedium);
        sizeLargeCheckBox = (CheckBox) rootView.findViewById(R.id.checkboxSizeLarge);
        //sizeAllCheckBox.setOnClickListener(this);
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
                boolean correctDatesInput = true;
                boolean correctSizeInput = true;
                boolean correctTypeInput = true;
                boolean correctTextInput = true;

                /*if(!verified)*/ fillGroupIds();
                description = descriptionEditText.getText().toString();
                reward = rewardEditText.getText().toString();
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
                   if(!verified){
                       start = new DateTime(startDate);
                       end = new DateTime(endDate);
                       Log.d(TAG, "startDate:"+start.toString());
                       Log.d(TAG, "endDate:"+end.toString());
                       Log.d(TAG, "des: "+description);
                       Log.d(TAG, "reward: "+reward);
                       Log.d(TAG, "venueId: "+fsVenueId);
                       for(String s:types){
                           Log.d(TAG, "type: "+s);
                       }
                       for(String s:sizes){
                           Log.d(TAG, "size: "+s);
                       }
                       for(Long s:groupIds){
                           Log.d(TAG, "group: "+s);
                       }




                   }

                }


            }
        });
        return rootView;
    }


    private void fillGroupIds(){
        for(int i = 0;i<groupsListView.getChildCount();i++)
        {
            View view = groupsListView.getChildAt(i);
            CheckedTextView cv =(CheckedTextView)view.findViewById(R.id.checkedTextViewGroup);
            if(cv.isChecked())
            {
                groupIds.add(adapter.getItem(i).getGroupId());
            }
        }
    }

    /*

    Check user input
     */
    private boolean correctDates(){
        if(startDate!=null && endDate != null){
            return (startDate.before(endDate) ||
                    (!startDate.after(endDate) && startHour*60+startMinute<endHour*60+endMinute));}
        else return false;
    }
    private boolean correctGroupSize(){
        //incorrect if no item are checked
       return (sizeAll || sizeIndividual|| sizeSmall|| sizeMedium|| sizeLarge);
    }
    private boolean correctGroupTypes(){
        //incorrect if no item are checked
        return (typeAll|| typeFriends|| typeClub|| typeStudentGroup);
    }
    private boolean correctTextInput(){
        //TODO: check user input for event description and event reward
        return true;
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        // Check which checkbox was clicked
        switch(view.getId()) {
            //size
            //case R.id.checkboxSizeAll:
            //        sizeAll = !sizeAll;
            //    break;
            case R.id.checkboxSizeIndividual:
                sizeIndividual = !sizeIndividual;
                if(checked) sizes.add("Individual");
                else sizes.remove("Individual");
                break;
            case R.id.checkboxSizeSmall:
               sizeSmall = !sizeSmall;
                if(checked) sizes.add("Small");
                else sizes.remove("Small");
                break;
            case R.id.checkboxSizeMedium:
                sizeMedium = !sizeMedium;
                if(checked) sizes.add("Medium");
                else sizes.remove("Medium");
                break;
            case R.id.checkboxSizeLarge:
                sizeLarge = !sizeLarge;
                if(checked) sizes.add("Large");
                else sizes.remove("Large");
                break;
           //type
           // case R.id.checkboxTypeAll:
           //     typeAll = !typeAll;
           //     break;
            case R.id.checkboxTypeClub:
                typeClub = !typeClub;
                if(checked) types.add("Club");
                else types.remove("Club");
                break;
            case R.id.checkboxTypeFriends:
                typeFriends = !typeFriends;
                if(checked) types.add("Friends");
                else types.remove("Friends");
                break;
            case R.id.checkboxTypeStudentGroup:
                typeStudentGroup = !typeStudentGroup;
                if(checked) types.add("Studentgroup");
                else types.remove("Studentgroup");
                break;

            //verified venue
            case R.id.checkBoxVerified:
                verified = !verified;
                break;
        }
    }


    /*
        Date and time on click listeners initializing

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
                        endTimeEditText.setText(hour+":"+minute+":00");
                    }

                },newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);

                endTimeDialog.show();

            }
        });
    }


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
                Log.d(TAG, "amount of groups : " + groups.getGroups().size());

                for(int i=0;i<groups.getGroups().size();i++){
                    Log.d(TAG, "Group: "+groups.getGroups().get(i).toString());
                }

                adapter = new NewEventGroupListAdapter(getActivity(),groups.getGroups());

                groupsListView.setAdapter(adapter);
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


    public void selectedGroups(){
        for(int i=0;i<adapter.getCount();i++){

        }
    }
}
