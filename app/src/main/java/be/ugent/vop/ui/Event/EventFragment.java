package be.ugent.vop.ui.Event;

import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import be.ugent.vop.R;

/**
 * Created by vincent on 23/03/15.
 */
public class EventFragment extends Fragment {

    Button createButton;
    DatePicker startDatePicker, endDatePicker;
    TimePicker startTimePicker, endTimePicker;
    EditText descriptionEditText, rewardEditText;
    CheckBox typeAllCheckBox, typeFriendsCheckBox, typeClubCheckbox, typeStudentGroupCheckBox;
    CheckBox sizeAllCheckBox, sizeIndividualCheckBox, sizeSmallCheckBox, sizeMediumCheckBox, sizeLargeCheckBox;

    String venueId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        rootView = inflater.inflate(R.layout.fragment_new_event, container, false);

        return rootView;
    }

}
