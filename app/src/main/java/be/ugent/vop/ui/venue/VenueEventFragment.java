package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import be.ugent.vop.R;

public class VenueEventFragment extends Fragment {
    private String fsVenueId;
    private Activity context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue_event, container, false);

        context = getActivity();

        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getArguments().getString(VenueActivity.VENUE_ID);

        return rootView;
    }
}
